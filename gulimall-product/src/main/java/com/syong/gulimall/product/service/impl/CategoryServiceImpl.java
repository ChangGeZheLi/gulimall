package com.syong.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.syong.gulimall.product.service.CategoryBrandRelationService;
import com.syong.gulimall.product.vo.foregroundVo.Catalog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private RedissonClient redissonClient;

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
            menu.setChildren(getChildren(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
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
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
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

    /**
     * 添加了缓存的查询三级分类数据
     * lettuce、jedis都是操作redis的底层客户端，而redisTemplate对这两个工具的再次封装
     **/
    //TODO 产生堆外内存溢出：java.lang.OutOfMemoryError
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {

        //添加redis缓存,缓存中存放的是json字符串
        //json可以跨平台兼容
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            //缓存中没有，需要从数据库中查
            Map<String, List<Catalog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();

            return catalogJsonFromDb;
        }

        //缓存中有数据，则需要将JSON格式数据进行逆转
        //复杂对象转换需要typeReference
        TypeReference<Map<String, List<Catalog2Vo>>> typeReference = new TypeReference<>() {
        };

        Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, typeReference);

        return result;
    }

    /**
     * 从数据库查询并封装整个分类数据使用Redisson完成分布式锁
     * 缓存和数据库中数据一致性问题
     *      双写模式
     *      失效模式
     **/
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        //获取Redsson分布式锁
        RLock lock = redissonClient.getLock("CatalogJson-lock");
        lock.lock();

        //加锁成功
        Map<String, List<Catalog2Vo>> dataFromDB;
        try {
            dataFromDB = getDataFromDB();
        } finally {
            lock.unlock();
        }
        return dataFromDB;
    }


    /**
     * 从数据库查询并封装整个分类数据使用redis的sentnx完成分布式锁
     **/
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        //添加redis分布式锁,并且设置过期时间，避免死锁情况
        //添加uuid，避免删除锁时候，因为时间过长，自己的锁已经自动删除了，结果删除的是别人的锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);

        //判断是否加锁成功
        if (lock) {
            //加锁成功
            Map<String, List<Catalog2Vo>> dataFromDB;
            //判断是否是自己的锁，才删除
            //获取redis的value值进行对比然后删除应该是一个原子操作
            //所以使用redis官方脚本
            try {
                dataFromDB = getDataFromDB();
            } finally {
                //无论是否异常都进行解锁
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                //执行官方脚本
                redisTemplate.execute(
                        new DefaultRedisScript<Long>(script, Long.class)
                        , Arrays.asList("lock"), uuid);
            }
            return dataFromDB;
        } else {
            //加锁失败，以自旋的方式重试
            return getCatalogJsonFromDbWithRedisLock();
        }

    }

    private Map<String, List<Catalog2Vo>> getDataFromDB() {
        //在查数据库之前先查缓存有没有数据
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });

            return result;
        }

        //将多次查询数据库优化为一次
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //查出所有的一级分类
        List<CategoryEntity> level1Categories = getParentCid(selectList, 0L);

        //封装数据
        Map<String, List<Catalog2Vo>> parent_cid = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            //将上面结果封装为指定格式
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //找出当前二级分类的三级分类
                    List<CategoryEntity> category2Entities = getParentCid(selectList, l2.getCatId());
                    if (category2Entities != null) {
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

        //将数据库中的数据存放到缓存中,先将数据转换为JSON格式
        String s = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);

        return parent_cid;
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectList, Long parentCid) {
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
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

}