package com.syong.gulimall.product.vo.foregroundVo;

import com.syong.gulimall.product.entity.SkuImagesEntity;
import com.syong.gulimall.product.entity.SkuInfoEntity;
import com.syong.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 封装商品详情页数据
 */
@Data
public class SkuItemVo {
    /**
     * sku基本信息获取
     **/
    private SkuInfoEntity skuInfo;
    /**
     * sku的图片信息
     **/
    private List<SkuImagesEntity> images;
    /**
     * 商品是否有货
     **/
    private Boolean hasStock  = true;
    /**
     * spu的销售属性组合信息
     **/
    List<SkuItemSaleAttrVo> saleAttrs;
    /**
     * spu的介绍
     **/
    private SpuInfoDescEntity spuDesc;
    /**
     * spu的规格参数信息
     **/
    private List<SpuItemAttrGroupVo> groupAttrs;

}
