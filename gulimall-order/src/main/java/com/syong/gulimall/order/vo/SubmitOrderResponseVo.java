package com.syong.gulimall.order.vo;

import com.syong.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @Description: 封装提交订单后返回数据
 */
@Data
public class SubmitOrderResponseVo {
    /**
     * 订单信息
     **/
    private OrderEntity order;
    /**
     * 错误状态码，0-成功
     **/
    private Integer code;
}
