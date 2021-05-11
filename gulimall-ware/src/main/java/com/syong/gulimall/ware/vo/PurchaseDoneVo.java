package com.syong.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description:
 */
@Data
public class PurchaseDoneVo {

    /**
     * id: 123,//采购单id
     **/
    @NotNull
    private Long id;
    /**
     * [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
     **/
    private List<PurchaseItemDoneVo> items;
}
