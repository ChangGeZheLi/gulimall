package com.syong.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description: 需要锁定的库存信息
 */
@Data
public class WareSkuLockVo {
    /**
     * 订单号
     **/
    private String orderSn;
    /**
     * 需要锁定的所有库存信息
     **/
    private List<OrderItemVo> locks;
}
