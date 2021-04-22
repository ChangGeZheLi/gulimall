package com.syong.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.syong.common.constant.ProductConstant;
import com.syong.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.syong.gulimall.product.dao.AttrGroupDao;
import com.syong.gulimall.product.dao.CategoryDao;
import com.syong.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.syong.gulimall.product.entity.AttrGroupEntity;
import com.syong.gulimall.product.entity.CategoryEntity;
import com.syong.gulimall.product.service.CategoryService;
import com.syong.gulimall.product.vo.AttrGroupRelationVo;
import com.syong.gulimall.product.vo.AttrResponseVo;
import com.syong.gulimall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.product.dao.AttrDao;
import com.syong.gulimall.product.entity.AttrEntity;
import com.syong.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Resource
    private AttrGroupDao attrGroupDao;
    @Resource
    private CategoryDao categoryDao;
    @Resource
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity entity = new AttrEntity();
        //使用Spring中的BeanUtils将页面数据封装到PO中
        BeanUtils.copyProperties(attr, entity);
        //保存基本数据
        this.save(entity);

        //保存属性和属性分组之间的关联表中的信息
        //只有是基本属性才保存到关联表
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(entity.getAttrId());

            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    /**
     * 获取包含所属分类和所属分组的完整规格参数
     **/
    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        //在一开始就直接将type构建进queryWrapper
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type", "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE : ProductConstant.AttrEnum.ATTR_TYPE_SALE);
        //拼接查询条件
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }

        //前端有传递模糊查询字段
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            //考虑到catelogId不为0情况，需要拼接and
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);

        //得到的数据不够，需要查询所属分类和所属分组的数据
        PageUtils pageUtils = new PageUtils(page);
        //返回所查到的数据列表
        List<AttrEntity> records = page.getRecords();
        List<AttrResponseVo> responseVos = records.stream().map((attrEntity) -> {
            //返回数据应该是一个AttrResponseVo对象
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            //存入基本数据
            BeanUtils.copyProperties(attrEntity, attrResponseVo);

            if ("base".equalsIgnoreCase(type)) {
                //查询属性分组name
                AttrAttrgroupRelationEntity attrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                //防止关联信息null
                if (attrgroupRelationEntity != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupRelationEntity.getAttrGroupId());
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            //查询属性分类name
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrResponseVo.setCatelogName(categoryEntity.getName());
            }

            return attrResponseVo;
        }).collect(Collectors.toList());

//        System.out.println(responseVos);
        pageUtils.setList(responseVos);
        return pageUtils;
    }

    /**
     * 获取属性分组id以及完整分类路径
     **/
    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrResponseVo attrResponseVo = new AttrResponseVo();
        //查询属性详细信息
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrResponseVo);

        //只有是基本属性才查询分组
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //查询attrGroupId
            AttrAttrgroupRelationEntity attrgroupRelation = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            //防止数据为null
            if (attrgroupRelation != null) {
                attrResponseVo.setAttrGroupId(attrgroupRelation.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupRelation.getAttrGroupId());
                if (attrGroupEntity != null) {
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        //查询分类完整路径;复用categoryService中的方法
        Long[] path = categoryService.getCatalogPathById(attrEntity.getCatelogId());
        attrResponseVo.setCatelogPath(path);
        CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
        if (categoryEntity != null) {
            attrResponseVo.setCatelogName(categoryEntity.getName());
        }

        return attrResponseVo;
    }

    /**
     * 更新包含有分类和属性分组信息的表
     **/
    @Transactional
    @Override
    public void updateAttr(AttrResponseVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        //修改基本数据
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //修改关联分组表
            AttrAttrgroupRelationEntity attrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            //修改字段数据存入对象
            attrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrgroupRelationEntity.setAttrId(attr.getAttrId());

            //selectCount 根据 Wrapper 条件，查询总记录数
            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            //只有count>0才进行修改操作，否则进行新增操作
            if (count > 0) {
                attrAttrgroupRelationDao.update(attrgroupRelationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            } else {
                attrAttrgroupRelationDao.insert(attrgroupRelationEntity);
            }
        }

    }

    /**
     * 根据分组id查找关联的所有基本属性
     **/
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));

        //将所有的attrId收集
        List<Long> attrIds = relationEntities.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        //根据属性id集合查询所有属性
        List<AttrEntity> attrEntities = this.listByIds(attrIds);

        return attrEntities;
    }

    /**
     * 传递id删除关联关系
     **/
    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        //一次性拼接所有删除请求
        List<AttrAttrgroupRelationEntity> relationEntities = Arrays.asList(vos).stream().map((item) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());

        attrAttrgroupRelationDao.deleteBatchRelation(relationEntities);
    }

}