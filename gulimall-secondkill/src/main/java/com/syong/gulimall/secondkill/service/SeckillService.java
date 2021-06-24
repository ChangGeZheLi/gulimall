package com.syong.gulimall.secondkill.service;

import com.syong.gulimall.secondkill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * @Description:
 */
public interface SeckillService {

    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
