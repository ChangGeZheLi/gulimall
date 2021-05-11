package com.syong.gulimall.ware.dao;

import com.syong.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存
 * 
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 16:34:14
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

}
