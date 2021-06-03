package com.syong.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syong.common.utils.PageUtils;
import com.syong.gulimall.product.entity.AttrGroupEntity;
import com.syong.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.syong.gulimall.product.vo.foregroundVo.SpuItemAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 11:52:17
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    List<AttrGroupWithAttrsVo> getAttrGourpWithAttrsByCatelogId(Long catelogId);

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

