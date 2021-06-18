package com.syong.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 */
@Data
public class SkuWareHasStock {
    private Long skuId;
    private Integer num;
    private List<Long> wareId;
}

