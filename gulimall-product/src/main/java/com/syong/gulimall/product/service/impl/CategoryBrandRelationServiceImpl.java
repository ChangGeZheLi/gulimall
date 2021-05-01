package com.syong.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.syong.gulimall.product.dao.BrandDao;
import com.syong.gulimall.product.dao.CategoryDao;
import com.syong.gulimall.product.entity.BrandEntity;
import com.syong.gulimall.product.entity.CategoryEntity;
import com.syong.gulimall.product.service.BrandService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.product.dao.CategoryBrandRelationDao;
import com.syong.gulimall.product.entity.CategoryBrandRelationEntity;
import com.syong.gulimall.product.service.CategoryBrandRelationService;

import javax.annotation.Resource;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Resource
    private BrandDao brandDao;
    @Resource
    private CategoryDao categoryDao;
    @Resource
    private CategoryBrandRelationDao categoryBrandRelationDao;
    @Resource
    private BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();

        //查询详细品牌name
        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());

        this.save(categoryBrandRelation);
    }

    /**
     * 如果品牌id和品牌name更新，需要更新关联表中的字段
     **/
    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
        //将需要更新的字段放进对象
        entity.setBrandId(brandId);
        entity.setBrandName(name);
        //eq("brand_id",brandId)，拼接更新条件
        this.update(entity,new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId));
    }

    @Override
    public void updateCategory(Long catelogId, String name) {
//        categoryBrandRelationDao.updateCategory(catId,name);
        CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
        entity.setCatelogId(catelogId);
        entity.setCatelogName(name);
        this.update(entity,new UpdateWrapper<CategoryBrandRelationEntity>().eq("catelog_id",catelogId));
    }

    /**
     * 查询指定分类下的所有品牌信息
     **/
    @Override
    public List<BrandEntity> getBrandByCatId(Long catId) {
        List<CategoryBrandRelationEntity> entities = categoryBrandRelationDao.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        List<BrandEntity> collect = entities.stream().map(item -> {
            Long brandId = item.getBrandId();
            BrandEntity byId = brandService.getById(brandId);

            return byId;
        }).collect(Collectors.toList());


        return collect;
    }

}
