package com.syong.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syong.common.to.mq.StockLockedTo;
import com.syong.common.utils.PageUtils;
import com.syong.gulimall.ware.entity.WareSkuEntity;
import com.syong.gulimall.ware.vo.SkuHasStockVo;
import com.syong.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 16:34:14
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> hasStock(List<Long> skuIds);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);

    void unlockStock(StockLockedTo to);
}

