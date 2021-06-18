package com.syong.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description: 订单提交数据封装
 */
@Data
public class OrderSubmitVo {
    /**
     * 收货地址id
     **/
    private Long addressId;
    /**
     * 支付方式
     **/
    private Integer payType;
    /**
     * 放重令牌
     **/
    private String orderToken;
    /**
     * 应付价格，方便验价
     **/
    private BigDecimal payPrice;
}
