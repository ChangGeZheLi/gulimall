package com.syong.gulimall.order.to;

import com.syong.gulimall.order.entity.OrderEntity;
import com.syong.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: 创建订单需要数据
 */
@Data
public class OrderCreateTo {
    /**
     * 订单信息
     **/
    private OrderEntity order;
    /**
     * 订单项信息
     **/
    private List<OrderItemEntity> orderItems;
    /**
     * 订单应付价格
     **/
    private BigDecimal payPrice;
    /**
     * 运费
     **/
    private BigDecimal fare;
}
