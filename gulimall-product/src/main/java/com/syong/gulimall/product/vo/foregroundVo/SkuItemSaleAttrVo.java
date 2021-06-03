package com.syong.gulimall.product.vo.foregroundVo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Description:
 */
@Data
@ToString
public class SkuItemSaleAttrVo{
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}