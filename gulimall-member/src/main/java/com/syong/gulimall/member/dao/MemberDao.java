package com.syong.gulimall.member.dao;

import com.syong.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 会员
 * 
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 16:18:36
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {

}
