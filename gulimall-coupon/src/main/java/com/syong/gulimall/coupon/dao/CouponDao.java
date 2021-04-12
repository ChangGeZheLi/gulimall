package com.syong.gulimall.coupon.dao;

import com.syong.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 16:05:12
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
