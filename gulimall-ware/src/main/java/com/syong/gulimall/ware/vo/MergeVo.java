package com.syong.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 */
@Data
public class MergeVo {
    /**
     * purchaseId: 1, //整单id
     **/
    private Long purchaseId;
    /**
     * items:[1,2,3,4] //合并项集合
     **/
    private List<Long> items;
}
