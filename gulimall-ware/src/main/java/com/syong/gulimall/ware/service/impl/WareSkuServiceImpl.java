package com.syong.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.syong.common.utils.R;
import com.syong.gulimall.ware.feign.ProductFeignService;
import com.syong.gulimall.ware.vo.SkuHasStockVo;
import org.springframework.beans.factory.annotation.Autowired;
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

}