package com.syong.gulimall.member.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.member.dao.MemberLevelDao;
import com.syong.gulimall.member.entity.MemberLevelEntity;
import com.syong.gulimall.member.service.MemberLevelService;


@Service("memberLevelService")
public class MemberLevelServiceImpl extends ServiceImpl<MemberLevelDao, MemberLevelEntity> implements MemberLevelService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberLevelEntity> page = this.page(
                new Query<MemberLevelEntity>().getPage(params),
                new QueryWrapper<MemberLevelEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询会员的默认等级
     **/
    @Override
    public MemberLevelEntity getDefaultLevel() {

        MemberLevelEntity memberLevelEntity = this.baseMapper.selectOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));

//        MemberLevelEntity memberLevelEntity = this.baseMapper.getDefaultLevel();
        return memberLevelEntity;
    }

}