package com.syong.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.syong.common.exception.NoStockException;
import com.syong.common.utils.R;
import com.syong.gulimall.ware.feign.ProductFeignService;
import com.syong.gulimall.ware.vo.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.ware.dao.WareSkuDao;
import com.syong.gulimall.ware.entity.WareSkuEntity;
import com.syong.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private WareSkuDao wareSkuDao;
    @Resource
    private ProductFeignService feignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        /**
         * 添加查询条件
         * skuId: 1
         * wareId: 1
         **/
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)){
            wrapper.eq("sku_id",skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)){
            wrapper.eq("ware_id",wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //判断如果没有库存记录则是新增操作
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities == null || wareSkuEntities.size() == 0){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);

            try{
                //远程查询skuName
                R info = feignService.info(skuId);
                Map<String,Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
            }catch (Exception e){
                e.printStackTrace();
            }


            wareSkuDao.insert(wareSkuEntity);
        }else {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setStock(wareSkuEntities.get(0).getStock() + skuNum);

            this.update(skuEntity,new UpdateWrapper<WareSkuEntity>().eq("sku_id",skuId).eq("ware_id",wareId));
        }

    }

    @Override
    public List<SkuHasStockVo> hasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(sku -> {
            SkuHasStockVo vo = new SkuHasStockVo();

            //查询当前sku库存量
            Long count = baseMapper.getSkuStock(sku);

            vo.setSkuId(sku);
            vo.setHasStock(count==null?false:count>0);

            return vo;
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();

            //查询当前sku的总库存量
            Long skuStock = baseMapper.getSkuStock(skuId);
            vo.setHasStock(skuStock == null ? false : skuStock > 0);
            vo.setSkuId(skuId);
            return vo;
        }).collect(Collectors.toList());

        return collect;
    }

    /**
     * 为商品锁定库存
     *
     * @return*/
    @Override
    @Transactional(rollbackFor = NoStockException.class)
    public Boolean orderLockStock(WareSkuLockVo vo) {

        //按照下单的收货地址，找到一个就近仓库，锁定库存（不做）

        //找到每个商品在哪个仓库有货
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();

            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());

            //查询指定商品在哪个仓库有库存
            List<Long> wareIds = this.baseMapper.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);

            return stock;
        }).collect(Collectors.toList());

        boolean allLock = true;
        //锁定库存
        for (SkuWareHasStock stock : collect) {

            boolean skuLocked = false;

            Long skuId = stock.getSkuId();
            List<Long> wareIds = stock.getWareId();

            //没有任何仓库有该库存就抛异常
            if (wareIds == null && wareIds.size() == 0){
                throw new NoStockException(skuId);
            }

            for (Long wareId : wareIds) {
                //成功就返回1，否则为0
                long count = this.baseMapper.lockSkuStock(skuId,wareId,stock.getNum());
                if (count == 0){
                    skuLocked = true;
                    break;
                }else {
                    //当前仓库锁定失败，重试下一个仓库
                }
            }

            if (!skuLocked){
                //当前商品所有仓库都没锁到库存
                throw new NoStockException(skuId);
            }
        }

        return true;
    }

}