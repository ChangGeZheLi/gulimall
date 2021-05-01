package com.syong.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syong.common.to.SkuReductionTo;
import com.syong.common.utils.PageUtils;
import com.syong.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 16:05:12
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

