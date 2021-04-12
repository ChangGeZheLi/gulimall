package com.syong.gulimall.order.dao;

import com.syong.gulimall.order.entity.MqMessageEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 16:26:33
 */
@Mapper
public interface MqMessageDao extends BaseMapper<MqMessageEntity> {
	
}
