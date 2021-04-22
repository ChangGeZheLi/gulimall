package com.syong.gulimall.product.service.impl;

import com.syong.gulimall.product.service.CategoryBrandRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.product.dao.CategoryDao;
import com.syong.gulimall.product.entity.CategoryEntity;
import com.syong.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * ServiceImpl<M extends BaseMapper<T>, T>
     * protected M baseMapper;
     * baseMapper代表泛型中的类
     **/
    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查询所有数据,传入null表示查所有
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、将数据组装成父子结构、
        //2.1找出所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter((categoryEntity) -> {
            return categoryEntity.getParentCid() == 0;
        }).map((menu) -> {
            menu.setChildren(getChildren(menu,entities));
            return menu;
        }).sorted((menu1,menu2) -> {
            return menu1.getSort() - menu2.getSort();
        }).collect(Collectors.toList());

        return level1Menus;
    }

    /**
     * 批量删除菜单
     **/
    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 检查当前删除的菜单，是否被别的地方引用

        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 找到catelogId的完整路径
     **/
    @Override
    public Long[] getCatalogPathById(Long catalogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> catalogPath = getCatalogPath(catalogId, paths);

        //使用集合工具类将得到结果进行反转
        Collections.reverse(catalogPath);

        return catalogPath.toArray(new Long[catalogPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     **/
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
//        int i = categoryDao.updateById(category);
//        log.info("更新是否成功{}",i);
//        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());

        this.updateById(category);
        if(!StringUtils.isEmpty(category.getName())){
            categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
        }

    }

    /**
     * 递归查找掉所有的父id
     **/
    private List<Long> getCatalogPath(Long catalogId, List<Long> paths) {
        //使用容器存储查询到的父id
        paths.add(catalogId);
        CategoryEntity id = getById(catalogId);
        if (id.getParentCid() != 0) {
            getCatalogPath(id.getParentCid(), paths);
        }
        return paths;
    }

    /**
     * 递归查找子菜单
     **/
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == root.getCatId()
        ).map(categoryEntity -> {
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1,menu2) -> {
            return (menu1.getSort() == null? 0 : menu1.getSort()) - (menu2.getSort() == null? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

}