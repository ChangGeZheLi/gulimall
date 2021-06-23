package com.syong.gulimall.coupon.service.impl;

import com.syong.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.syong.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.coupon.dao.SeckillSessionDao;
import com.syong.gulimall.coupon.entity.SeckillSessionEntity;
import com.syong.gulimall.coupon.service.SeckillSessionService;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Resource
    private SeckillSkuRelationService skuRelationService;

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

    /**
     * 查询最近三天需要上架的秒杀商品
     **/
    @Override
    public List<SeckillSessionEntity> getLatest3DaySession() {

        List<SeckillSessionEntity> sessionEntities = this.list(new QueryWrapper<SeckillSessionEntity>()
                .between("start_time",getStartTime(),getEndTime()));

        if (sessionEntities!=null && sessionEntities.size()>0){
            List<SeckillSessionEntity> collect = sessionEntities.stream().map(session -> {

                Long id = session.getId();
                List<SeckillSkuRelationEntity> entities = skuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", id));

                session.setRelationSkus(entities);

                return session;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    private String getStartTime(){
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        String format = LocalDateTime.of(now, min).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
         return format;
    }

    private String getEndTime(){
        LocalDate now = LocalDate.now();
        LocalDate plusDays = now.plusDays(2);
        LocalTime max = LocalTime.MAX;
        String format = LocalDateTime.of(plusDays, max).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

}