package com.syong.gulimall.ware.vo;

import lombok.Data;

/**
 * @Description: 完成采购传递的数据封装
 */
@Data
public class PurchaseItemDoneVo {
    /**
     * items: [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
     **/
    private Long itemId;
    private Integer status;
    private String reason;
}
