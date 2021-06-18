package com.syong.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.syong.common.utils.R;
import com.syong.common.vo.MemberEntity;
import com.syong.gulimall.order.constant.OrderConstant;
import com.syong.gulimall.order.entity.OrderItemEntity;
import com.syong.gulimall.order.enumm.OrderStatusEnum;
import com.syong.gulimall.order.feign.CartFeignService;
import com.syong.gulimall.order.feign.MemberFeignService;
import com.syong.gulimall.order.feign.ProductFeignService;
import com.syong.gulimall.order.feign.WareFeignService;
import com.syong.gulimall.order.interceptor.LoginInterceptor;
import com.syong.gulimall.order.service.OrderItemService;
import com.syong.gulimall.order.to.OrderCreateTo;
import com.syong.gulimall.order.vo.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.order.dao.OrderDao;
import com.syong.gulimall.order.entity.OrderEntity;
import com.syong.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> threadLocal = new ThreadLocal<>();

    @Resource
    private MemberFeignService memberFeignService;
    @Resource
    private CartFeignService cartFeignService;
    @Resource
    private WareFeignService wareFeignService;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private OrderItemService orderItemService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 封装订单确认页需要的数据
     **/
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberEntity memberEntity = LoginInterceptor.threadLocal.get();

        //解决使用异步时，threadLocal无法共享数据问题
        //先获取到之前的请求数据
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        //使用异步编排方式执行远程调用
        CompletableFuture<Void> getAddressTask = CompletableFuture.runAsync(() -> {
            //查询所有的收货地址
            //每一个异步线程都共享请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberEntity.getId());
            confirmVo.setAddresses(address);
        }, threadPoolExecutor);

        CompletableFuture<Void> getCartItemTask = CompletableFuture.runAsync(() -> {
            //远程查询购物项中所有选中的数据
            //每一个异步线程都共享请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> cartItems = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(cartItems);
        }, threadPoolExecutor).thenRunAsync(()->{
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R r = wareFeignService.getSkuHasStock(collect);
            List<SkuHasStockVo> data = r.getData(new TypeReference<List<SkuHasStockVo>>() {});

            if (data != null){
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));

                confirmVo.setStocks(map);
            }

        },threadPoolExecutor);

        //integration
        Integer integration = memberEntity.getIntegration();
        confirmVo.setIntegration(integration);

        //放重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        //将令牌存入redis
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberEntity.getId(),token,30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);

        //等所有任务完成才返回
        CompletableFuture.allOf(getAddressTask,getCartItemTask).get();

        return confirmVo;
    }

    /**
     * 提交订单
     **/
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {

        threadLocal.set(submitVo);

        MemberEntity memberEntity = LoginInterceptor.threadLocal.get();
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0);

        //防重令牌验证，必须要保证对比和删除是原子性操作
        String script= "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<>(script,Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberEntity.getId()), submitVo.getOrderToken());

        if (result == 0L){
            //令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        }{
            //令牌验证成功
            //下单，创建订单，验令牌，锁库存
            OrderCreateTo order = createOrder();
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = submitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue())<0.01){
                //验价成功
                //保存订单
                saveOrder(order);
                //锁定库存,加了事务有异常就会回滚数据
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());

                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setTitle(item.getSkuName());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setSkuId(item.getSkuId());

                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(locks);

                R r = wareFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode() == 0) {
                    //锁定成功
                    responseVo.setOrder(order.getOrder());
                    return responseVo;
                }else {
                    //失败
                    responseVo.setCode(3);
                    return responseVo;
                }
            }else {
                //验价失败
                responseVo.setCode(2);
                return responseVo;
            }
        }
    }

    /**
     * 保存订单
     **/
    private void saveOrder(OrderCreateTo order) {
        //保存订单
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setCreateTime(new Date());
        orderEntity.setModifyTime(new Date());
        this.baseMapper.insert(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);

    }

    /**
     * 创建订单
     **/
    private OrderCreateTo createOrder(){

        OrderCreateTo orderCreateTo = new OrderCreateTo();

        //生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);

        //获取到所有的订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);

        //计算价格相关
        computerPrice(orderEntity,itemEntities);
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(itemEntities);

        return orderCreateTo;
    }

    private void computerPrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");

        //优惠价格
        BigDecimal promotion=new BigDecimal("0.0");
        BigDecimal integration=new BigDecimal("0.0");
        BigDecimal coupon=new BigDecimal("0.0");
        //积分
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        //订单的总金额，叠加每一个订单项的总额
        for (OrderItemEntity entity : itemEntities) {
            promotion=promotion.add(entity.getPromotionAmount());
            integration=integration.add(entity.getIntegrationAmount());
            coupon=coupon.add(entity.getCouponAmount());
            total = total.add(entity.getRealAmount());
        }

        orderEntity.setTotalAmount(total);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //付款价格=商品价格+运费
        orderEntity.setPayAmount(orderEntity.getFreightAmount().add(total));

        orderEntity.setDeleteStatus(0);

    }

    private OrderEntity buildOrder(String orderSn) {
        MemberEntity memberEntity = LoginInterceptor.threadLocal.get();
        OrderSubmitVo orderSubmitVo = threadLocal.get();
        //创建订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);

        //设置会员id
        orderEntity.setMemberId(memberEntity.getId());

        //获取收货地址信息
        R fare = wareFeignService.getFare(orderSubmitVo.getAddressId());
        FareVo data = fare.getData(new TypeReference<FareVo>() {});
        //设置运费
        orderEntity.setFreightAmount(data.getFare());
        //设置收货人信息
        orderEntity.setReceiverCity(data.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(data.getAddress().getDetailAddress());
        orderEntity.setReceiverName(data.getAddress().getName());
        orderEntity.setReceiverPhone(data.getAddress().getPhone());
        orderEntity.setReceiverPostCode(data.getAddress().getPostCode());
        orderEntity.setReceiverProvince(data.getAddress().getProvince());
        orderEntity.setReceiverRegion(data.getAddress().getRegion());

        //4) 设置订单相关的状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setConfirmStatus(0);
        orderEntity.setAutoConfirmDay(7);

        return orderEntity;
    }

    /**
     * 构建所有订单项数据
     **/
    private List<OrderItemEntity> buildOrderItems(String orderSn){
        //获取到所有的订单项
        List<OrderItemVo> cartItems = cartFeignService.getCurrentUserCartItems();
        if (cartItems!=null && cartItems.size()>0){
            List<OrderItemEntity> collect = cartItems.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItem(item);

                orderItemEntity.setOrderSn(orderSn);

                return orderItemEntity;
            }).collect(Collectors.toList());
            return collect;
        }

        return null;
    }

    /**
     * 构建一个订单项
     **/
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        //商品spu信息
        Long skuId = item.getSkuId();
        R r = productFeignService.spuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {});
        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setCategoryId(data.getCatalogId());

        //商品sku信息
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);
        orderItemEntity.setSkuQuantity(item.getCount());

        //积分信息
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());

        //订单项的价格信息
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));

        //订单项实际金额
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal realPrice = origin.subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(realPrice);


        return orderItemEntity;
    }
}