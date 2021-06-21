package com.syong.gulimall.coupon.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.coupon.dao.SeckillSessionDao;
import com.syong.gulimall.coupon.entity.SeckillSessionEntity;
import com.syong.gulimall.coupon.service.SeckillSessionService;
import org.springframework.util.StringUtils;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSessionEntity> queryWrapper = new QueryWrapper<>();
        String sessionId = (String) params.get("promotionSessionId");

        if(!StringUtils.isEmpty(sessionId)){
            queryWrapper.eq("promotion_session_id",sessionId);
        }

        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

}