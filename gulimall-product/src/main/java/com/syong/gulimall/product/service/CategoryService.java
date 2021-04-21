package com.syong.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syong.common.utils.PageUtils;
import com.syong.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 11:52:17
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    Long[] getCatalogPathById(Long catelogId);

    void updateCascade(CategoryEntity category);
}

