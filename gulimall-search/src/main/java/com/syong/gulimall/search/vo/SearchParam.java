package com.syong.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description: 封装页面可能传递过来的搜索条件
 */
@Data
public class SearchParam {
    /**
     * 全文搜索关键字
     **/
    private String keyword;
    /**
     * 三级分类id
     **/
    private Long catalog3Id;
    /**
     * 排序条件
     * sort=saleCount_desc/asc
     * sort=skuPrice_desc/asc
     * sort=hotScore_desc/asc
     **/
    private String sort;
    /**
     * 过滤条件
     * 是否有货
     **/
    private Integer hasStock;
    /**
     * 价格区间
     **/
    private String skuPrice;
    /**
     * 按照品牌进行查询，可以多选
     **/
    private List<Long> brandId;
    /**
     * 按照属性进行帅选
     **/
    private List<String> attrs;
    /**
     * 页码
     **/
    private Integer pageNum;
}
