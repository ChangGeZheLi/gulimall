package com.syong.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.syong.common.utils.R;
import com.syong.common.vo.MemberEntity;
import com.syong.gulimall.order.feign.CartFeignService;
import com.syong.gulimall.order.feign.MemberFeignService;
import com.syong.gulimall.order.feign.WareFeignService;
import com.syong.gulimall.order.interceptor.LoginInterceptor;
import com.syong.gulimall.order.vo.MemberAddressVo;
import com.syong.gulimall.order.vo.OrderConfirmVo;
import com.syong.gulimall.order.vo.OrderItemVo;
import com.syong.gulimall.order.vo.SkuHasStockVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.order.dao.OrderDao;
import com.syong.gulimall.order.entity.OrderEntity;
import com.syong.gulimall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Resource
    private MemberFeignService memberFeignService;
    @Resource
    private CartFeignService cartFeignService;
    @Resource
    private WareFeignService wareFeignService;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

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

        //等所有任务完成才返回
        CompletableFuture.allOf(getAddressTask,getCartItemTask).get();

        return confirmVo;
    }

}