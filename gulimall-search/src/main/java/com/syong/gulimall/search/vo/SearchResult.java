package com.syong.gulimall.search.vo;

import com.syong.common.to.es.SkuESModel;
import lombok.Data;

import java.util.List;

/**
 * @Description:
 */
@Data
public class SearchResult {
    /**
     * 查询到的所有商品信息
     **/
    private List<SkuESModel> products;
    /**
     * 分页信息
     * 当前页码
     **/
    private Integer pageNum;
    /**
     * 总记录数
     **/
    private Long total;
    /**
     * 总页码
     **/
    private Integer totalPages;
    /**
     * 当前查询到的结果之中，所有涉及到的品牌
     **/
    private List<BrandVo> brands;
    /**
     * 当前查询到的结果之中，所有涉及到的属性
     **/
    private List<AttrVo> attrs;
    /**
     * 当前查询到的结果之中，所有涉及到的分类
     **/
    private List<CatalogVo> catalogs;


    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValues;
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }

}
