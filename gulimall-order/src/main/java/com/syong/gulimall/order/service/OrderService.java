package com.syong.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syong.common.utils.PageUtils;
import com.syong.gulimall.order.entity.OrderEntity;
import com.syong.gulimall.order.vo.OrderConfirmVo;
import com.syong.gulimall.order.vo.OrderSubmitVo;
import com.syong.gulimall.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 16:26:33
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderStatusByOrderSn(String orderSn);
}

