package com.syong.gulimall.ware.vo;

import lombok.Data;

/**
 * @Description: 封装商品锁定是否成功信息
 */
@Data
public class LockStockResultVo {
    /**
     * 锁定商品id
     **/
    private Long skuId;
    /**
     * 锁定商品数量
     **/
    private Integer num;
    /**
     * 锁定状态，是否成功
     **/
    private Boolean locked;
}
