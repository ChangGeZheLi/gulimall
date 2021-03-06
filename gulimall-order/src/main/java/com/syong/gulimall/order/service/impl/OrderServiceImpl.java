package com.syong.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.syong.common.to.mq.SeckillOrderTo;
import com.syong.common.utils.R;
import com.syong.common.vo.MemberEntity;
import com.syong.gulimall.order.constant.OrderConstant;
import com.syong.gulimall.order.entity.OrderItemEntity;
import com.syong.gulimall.order.entity.PaymentInfoEntity;
import com.syong.gulimall.order.enumm.OrderStatusEnum;
import com.syong.gulimall.order.feign.CartFeignService;
import com.syong.gulimall.order.feign.MemberFeignService;
import com.syong.gulimall.order.feign.ProductFeignService;
import com.syong.gulimall.order.feign.WareFeignService;
import com.syong.gulimall.order.interceptor.LoginInterceptor;
import com.syong.gulimall.order.service.OrderItemService;
import com.syong.gulimall.order.service.PaymentInfoService;
import com.syong.gulimall.order.to.OrderCreateTo;
import com.syong.common.to.OrderTo;
import com.syong.gulimall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
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
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * ????????????????????????????????????
     **/
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberEntity memberEntity = LoginInterceptor.threadLocal.get();

        //????????????????????????threadLocal????????????????????????
        //?????????????????????????????????
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        //??????????????????????????????????????????
        CompletableFuture<Void> getAddressTask = CompletableFuture.runAsync(() -> {
            //???????????????????????????
            //??????????????????????????????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberEntity.getId());
            confirmVo.setAddresses(address);
        }, threadPoolExecutor);

        CompletableFuture<Void> getCartItemTask = CompletableFuture.runAsync(() -> {
            //?????????????????????????????????????????????
            //??????????????????????????????????????????
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

        //????????????
        String token = UUID.randomUUID().toString().replace("-", "");
        //???????????????redis
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberEntity.getId(),token,30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);

        //??????????????????????????????
        CompletableFuture.allOf(getAddressTask,getCartItemTask).get();

        return confirmVo;
    }

    /**
     * ????????????
     **/
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {

        threadLocal.set(submitVo);

        MemberEntity memberEntity = LoginInterceptor.threadLocal.get();
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0);

        //?????????????????????????????????????????????????????????????????????
        String script= "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<>(script,Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberEntity.getId()), submitVo.getOrderToken());

        if (result == 0L){
            //??????????????????
            responseVo.setCode(1);
            return responseVo;
        }{
            //??????????????????
            //?????????????????????????????????????????????
            OrderCreateTo order = createOrder();
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = submitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue())<0.01){
                //????????????
                //????????????
                saveOrder(order);
                //????????????,???????????????????????????????????????
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
                    //????????????
                    responseVo.setOrder(order.getOrder());
                    //????????????????????????rabbitmq????????????
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());
                }else {
                    //??????
                    responseVo.setCode(3);
                }
                return responseVo;
            }else {
                //????????????
                responseVo.setCode(2);
                return responseVo;
            }
        }
    }

    @Override
    public OrderEntity getOrderStatusByOrderSn(String orderSn) {

        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));

        return orderEntity;
    }

    /**
     * ????????????????????????????????????????????????????????????
     **/
    @Override
    public void closeOrder(OrderEntity entity) {
        //???????????????????????????????????????????????????
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())){
            //????????????
            OrderEntity order = new OrderEntity();
            order.setId(entity.getId());
            order.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(order);

            //??????mq???????????????????????????????????????
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity,orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange","order.release.other.#",orderTo);
        }
    }

    /**
     * ?????????????????????????????????
     **/
    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();

        OrderEntity orderEntity = this.getOrderStatusByOrderSn(orderSn);
        payVo.setTotal_amount(orderEntity.getPayAmount().setScale(2,BigDecimal.ROUND_UP).toString());
        payVo.setOut_trade_no(orderSn);

        List<OrderItemEntity> itemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity itemEntity = itemEntities.get(0);

        payVo.setSubject(itemEntity.getSkuName());
        payVo.setBody(itemEntity.getSkuAttrsVals());

        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {

        MemberEntity memberEntity = LoginInterceptor.threadLocal.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id",memberEntity.getId()).orderByDesc("id")
        );

        List<OrderEntity> collect = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> entityList = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));

            order.setItemEntities(entityList);

            return order;
        }).collect(Collectors.toList());

        page.setRecords(collect);

        return new PageUtils(page);

    }

    /**
     * ?????????????????????????????????,??????????????????
     **/
    @Override
    public String handlePayResult(PayAsyncVo vo) {

        //??????????????????
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setAlipayTradeNo(vo.getTrade_no());
        paymentInfoEntity.setCallbackTime(vo.getNotify_time());
        paymentInfoEntity.setOrderSn(vo.getOut_trade_no());
        paymentInfoEntity.setPaymentStatus(vo.getTrade_status());

        paymentInfoService.save(paymentInfoEntity);

        //???????????????????????????
        if (vo.getTrade_status().equals("TRADE_SUCCESS")||vo.getTrade_status().equals("TRADE_FINISHED")){
            String outTradeNo = vo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(outTradeNo,OrderStatusEnum.PAYED.getCode());
        }

        return "success";
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo to) {
        //??????????????????
        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setOrderSn(to.getOrderSn());
        orderEntity.setMemberId(to.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        BigDecimal multiply = to.getSeckillPrice().multiply(new BigDecimal("" + to.getNum()));
        orderEntity.setPayAmount(multiply);

        this.save(orderEntity);

        //?????????????????????
        OrderItemEntity itemEntity = new OrderItemEntity();

        itemEntity.setOrderSn(to.getOrderSn());
        itemEntity.setRealAmount(multiply);
        itemEntity.setSkuQuantity(to.getNum());

        orderItemService.save(itemEntity);
    }

    /**
     * ????????????
     **/
    private void saveOrder(OrderCreateTo order) {
        //????????????
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setCreateTime(new Date());
        orderEntity.setModifyTime(new Date());
        this.baseMapper.insert(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);

    }

    /**
     * ????????????
     **/
    private OrderCreateTo createOrder(){

        OrderCreateTo orderCreateTo = new OrderCreateTo();

        //???????????????
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);

        //???????????????????????????
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);

        //??????????????????
        computerPrice(orderEntity,itemEntities);
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(itemEntities);

        return orderCreateTo;
    }

    private void computerPrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");

        //????????????
        BigDecimal promotion=new BigDecimal("0.0");
        BigDecimal integration=new BigDecimal("0.0");
        BigDecimal coupon=new BigDecimal("0.0");
        //??????
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        //??????????????????????????????????????????????????????
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

        //????????????=????????????+??????
        orderEntity.setPayAmount(orderEntity.getFreightAmount().add(total));

        orderEntity.setDeleteStatus(0);

    }

    private OrderEntity buildOrder(String orderSn) {
        MemberEntity memberEntity = LoginInterceptor.threadLocal.get();
        OrderSubmitVo orderSubmitVo = threadLocal.get();
        //????????????
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);

        //????????????id
        orderEntity.setMemberId(memberEntity.getId());

        //????????????????????????
        R fare = wareFeignService.getFare(orderSubmitVo.getAddressId());
        FareVo data = fare.getData(new TypeReference<FareVo>() {});
        //????????????
        orderEntity.setFreightAmount(data.getFare());
        //?????????????????????
        orderEntity.setReceiverCity(data.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(data.getAddress().getDetailAddress());
        orderEntity.setReceiverName(data.getAddress().getName());
        orderEntity.setReceiverPhone(data.getAddress().getPhone());
        orderEntity.setReceiverPostCode(data.getAddress().getPostCode());
        orderEntity.setReceiverProvince(data.getAddress().getProvince());
        orderEntity.setReceiverRegion(data.getAddress().getRegion());

        //4) ?????????????????????????????????
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setConfirmStatus(0);
        orderEntity.setAutoConfirmDay(7);

        return orderEntity;
    }

    /**
     * ???????????????????????????
     **/
    private List<OrderItemEntity> buildOrderItems(String orderSn){
        //???????????????????????????
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
     * ?????????????????????
     **/
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        //??????spu??????
        Long skuId = item.getSkuId();
        R r = productFeignService.spuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {});
        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setCategoryId(data.getCatalogId());

        //??????sku??????
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);
        orderItemEntity.setSkuQuantity(item.getCount());

        //????????????
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());

        //????????????????????????
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));

        //?????????????????????
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal realPrice = origin.subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(realPrice);


        return orderItemEntity;
    }
}