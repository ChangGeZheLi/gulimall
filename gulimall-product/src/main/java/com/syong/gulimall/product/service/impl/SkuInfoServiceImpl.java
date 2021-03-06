package com.syong.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.syong.common.utils.R;
import com.syong.gulimall.product.entity.SkuImagesEntity;
import com.syong.gulimall.product.entity.SpuInfoDescEntity;
import com.syong.gulimall.product.feign.SeckillFeignService;
import com.syong.gulimall.product.service.*;
import com.syong.gulimall.product.vo.SeckillInfoVo;
import com.syong.gulimall.product.vo.foregroundVo.SkuItemSaleAttrVo;
import com.syong.gulimall.product.vo.foregroundVo.SkuItemVo;
import com.syong.gulimall.product.vo.foregroundVo.SpuItemAttrGroupVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.product.dao.SkuInfoDao;
import com.syong.gulimall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Resource
    private SkuImagesService skuImagesService;
    @Resource
    private SpuInfoDescService spuInfoDescService;
    @Resource
    private AttrGroupService attrGroupService;
    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByConditions(Map<String, Object> params) {

        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        /**
         * key: '??????',//???????????????
         * catelogId: 0,
         * brandId: 0,
         * min: 0,
         * max: 0
         **/
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
               w.eq("sku_id",key).or().like("sku_name",key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }else wrapper.eq("brand_id",9L);

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)){
            wrapper.ge("price",min);
        }

        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)){
            try{
                BigDecimal bigDecimal = new BigDecimal(max);
                //??????????????????????????????????????????????????????max???0??????
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1){
                    wrapper.le("price",max);
                }
            }catch (Exception e){
                e.printStackTrace();
            }


        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id",spuId));

        return list;
    }

    /**
     * ??????CompletableFuture??????????????????
     **/
    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //sku???????????? pms_sku_info
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setSkuInfo(info);
            return info;
        }, threadPoolExecutor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((result) -> {
            //??????spu?????????????????????
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(result.getSpuId());
            skuItemVo.setSaleAttrs(saleAttrVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> spuDescFuture = infoFuture.thenAcceptAsync((result) -> {
            //??????spu????????? pms_spu_info_desc
            SpuInfoDescEntity spuDesc = spuInfoDescService.getById(result.getSpuId());
            skuItemVo.setSpuDesc(spuDesc);
        }, threadPoolExecutor);

        CompletableFuture<Void> groupAttrsFuture = infoFuture.thenAcceptAsync((result) -> {
            //??????spu?????????????????????
            List<SpuItemAttrGroupVo> groupAttrs = attrGroupService.getAttrGroupWithAttrsBySpuId(result.getSpuId(), result.getCatalogId());
            skuItemVo.setGroupAttrs(groupAttrs);
        }, threadPoolExecutor);

        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            //sku???????????? pms_sku_images
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, threadPoolExecutor);

        //????????????sku????????????????????????
        CompletableFuture<Void> SeckillFuture = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.getSkuSeckillInfo(skuId);
            if (r.getCode() == 0) {
                SeckillInfoVo data = r.getData(new TypeReference<SeckillInfoVo>() {
                });
                skuItemVo.setSeckillInfo(data);
            }
        }, threadPoolExecutor);

        //????????????????????????????????????????????????skuItemVo,??????get()??????????????????????????????????????????????????????????????????????????????
        try {
            CompletableFuture.allOf(saleAttrFuture,spuDescFuture,groupAttrsFuture,imagesFuture,SeckillFuture).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return skuItemVo;
    }

}