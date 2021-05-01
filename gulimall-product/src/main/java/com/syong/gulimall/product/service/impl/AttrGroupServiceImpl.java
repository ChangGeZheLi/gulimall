package com.syong.gulimall.product.service.impl;

import com.syong.common.constant.ProductConstant;
import com.syong.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.syong.gulimall.product.dao.AttrDao;
import com.syong.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.syong.gulimall.product.entity.AttrEntity;
import com.syong.gulimall.product.service.AttrService;
import com.syong.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.product.dao.AttrGroupDao;
import com.syong.gulimall.product.entity.AttrGroupEntity;
import com.syong.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private AttrGroupDao attrGroupDao;
    @Resource
    private AttrAttrgroupRelationDao relationDao;
    @Resource
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        //select * from pms_attr_group where catelog_id = ? and (attr_group_id = key or attr_group_name like %key%)
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)){
            wrapper.and((obj)->{
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }
        //通过wrapper将查询条件进行拼接，this.page将查询结果进行封装，PageUtils(page)将结果进行解析
        //如果catelogId==0，则查询所有
        if (catelogId == 0){
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    wrapper);
            return new PageUtils(page);
        }else {
            wrapper.eq("catelog_id",catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    wrapper);
            return new PageUtils(page);
        }
    }

    /**
     * 获取当前分组没有关联的所有属性
     **/
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        //当前分组只能关联自己所属分类里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //当前分组只能关联别的分组没有的属性
        //当前分类下的其他分组
        List<AttrGroupEntity> groupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> collect = groupEntities.stream().map(item -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());

        //这些分组关联的属性
        List<AttrAttrgroupRelationEntity> groupId = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
        List<Long> attrIds = groupId.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());


        //从当前分类的所有属性中移除这些属性
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds != null && attrIds.size() > 0){
            wrapper.notIn("attr_id", attrIds);
        }
        //页面传递key值
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w)->{
                w.eq("attr_id",key).or().like("attr_name",key);
            });
        }

        IPage<AttrEntity> page = attrService.page(new Query<AttrEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    /**
     * 根据分类id查出所有分组以及分组下的所有属性
     **/
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGourpWithAttrsByCatelogId(Long catelogId) {
        //查询分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        //查询所有属性
        List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream().map(item -> {
            AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, vo);

            List<AttrEntity> attrs = attrService.getRelationAttr(vo.getAttrGroupId());
            vo.setAttrs(attrs);

            return vo;
        }).collect(Collectors.toList());

        return collect;
    }
}