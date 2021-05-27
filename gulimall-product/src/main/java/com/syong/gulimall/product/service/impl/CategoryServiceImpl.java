package com.syong.gulimall.product.service.impl;

import com.syong.gulimall.product.service.CategoryBrandRelationService;
import com.syong.gulimall.product.vo.foregroundVo.Catalog2Vo;
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
     * 查出所有的一级分类菜单
     **/
    @Override
    public List<CategoryEntity> getLevel1Categories() {

        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));

        return categoryEntities;
    }

    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {

        //将多次查询数据库优化为一次
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //查出所有的一级分类
        List<CategoryEntity> level1Categories = getParentCid(selectList,0L);

        //封装数据
        Map<String, List<Catalog2Vo>> parent_cid = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParentCid(selectList,v.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            //将上面结果封装为指定格式
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //找出当前二级分类的三级分类
                    List<CategoryEntity> category2Entities = getParentCid(selectList,l2.getCatId());
                    if (category2Entities != null){
                        List<Catalog2Vo.Catalog3Vo> collect = category2Entities.stream().map(l3 -> {
                            //将三级分类数据封装到catalog2Vo中
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(collect);
                    }

                    return catalog2Vo;
                }).collect(Collectors.toList());
            }

            return catalog2Vos;

        }));

        return parent_cid;
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectList,Long parentCid) {
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
        return collect;
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