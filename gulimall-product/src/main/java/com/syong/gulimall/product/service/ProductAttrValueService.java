package com.syong.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syong.common.utils.PageUtils;
import com.syong.gulimall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 11:52:16
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveProductAttr(List<ProductAttrValueEntity> collect);

    List<ProductAttrValueEntity> baseAttrlistForspu(Long spuId);

    void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities);
}

