package com.syong.gulimall.coupon.service.impl;

import com.syong.common.to.MemberPrice;
import com.syong.common.to.SkuReductionTo;
import com.syong.gulimall.coupon.entity.MemberPriceEntity;
import com.syong.gulimall.coupon.entity.SkuLadderEntity;
import com.syong.gulimall.coupon.service.MemberPriceService;
import com.syong.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.coupon.dao.SkuFullReductionDao;
import com.syong.gulimall.coupon.entity.SkuFullReductionEntity;
import com.syong.gulimall.coupon.service.SkuFullReductionService;

import javax.annotation.Resource;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Resource
    private SkuLadderService skuLadderService;
    @Resource
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {

        //sku的优惠、满减：gulimall_sms->sms_sku_ladder/sms_sku_full_reduction/sms_member_price
        //sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());

        if (skuReductionTo.getFullCount() > 0){
            skuLadderService.save(skuLadderEntity);
        }

        //sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo,skuFullReductionEntity);

        if (skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
            this.save(skuFullReductionEntity);
        }

        //sms_member_price
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrice.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
            memberPriceEntity.setMemberLevelId(item.getId());
            memberPriceEntity.setMemberLevelName(item.getName());
            memberPriceEntity.setMemberPrice(item.getPrice());
            memberPriceEntity.setAddOther(1);

            return memberPriceEntity;
        }).filter(item->{
           return item.getMemberPrice().compareTo(new BigDecimal("0")) == 1 ;
        }).collect(Collectors.toList());

        memberPriceService.saveBatch(collect);

    }

}