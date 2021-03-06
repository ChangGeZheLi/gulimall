package com.syong.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.syong.common.constant.ProductConstant;
import com.syong.common.to.SkuHasStockVo;
import com.syong.common.to.SkuReductionTo;
import com.syong.common.to.SpuBoundTo;
import com.syong.common.to.es.SkuESModel;
import com.syong.common.utils.R;
import com.syong.gulimall.product.entity.*;
import com.syong.gulimall.product.feign.CouponFeignService;
import com.syong.gulimall.product.feign.SearchFeignService;
import com.syong.gulimall.product.feign.WareFeignService;
import com.syong.gulimall.product.service.*;
import com.syong.gulimall.product.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Resource
    private SpuInfoDescService spuInfoDescService;
    @Resource
    private SpuImagesService spuImagesService;
    @Resource
    private AttrService attrService;
    @Resource
    private ProductAttrValueService productAttrValueService;
    @Resource
    private SkuInfoService skuInfoService;
    @Resource
    private SkuImagesService skuImagesService;
    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Resource
    private CouponFeignService couponFeignService;
    @Resource
    private BrandService brandService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private WareFeignService wareFeignService;
    @Resource
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    //TODO ???????????????
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //??????spu???????????????pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        spuInfoEntity.setBrandId(9L);
        this.saveBaseSpuInfo(spuInfoEntity);

        //??????spu??????????????????pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        //??????spu???????????????pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);

        //??????spu??????????????????pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            valueEntity.setId(spuInfoEntity.getId());
            //?????????????????????id??????
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(attrEntity.getAttrName());

            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());

            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);

        //??????spu??????????????????gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);

        //??????r???code???????????????????????????
//        if (r.getCode() != 0){
//            log.error("????????????spu??????????????????");
//        }

        //??????spu?????????sku??????
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0){
            skus.forEach(item->{

                //??????????????????url
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }

                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);

                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);

                //sku??????????????????pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    skuImagesEntity.setImgUrl(img.getImgUrl());

                    return skuImagesEntity;
                }).filter(entity->{
                    //??????true?????????????????????????????????
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());

                //sku????????????pms_sku_images
                //TODO ?????????????????????????????????
                skuImagesService.saveBatch(imagesEntities);

                //sku????????????????????????pms_sku_sale_attr_value
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = item.getAttr().stream().map(a -> {
                    SkuSaleAttrValueEntity saleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, saleAttrValueEntity);
                    saleAttrValueEntity.setSkuId(skuId);

                    return saleAttrValueEntity;
                }).collect(Collectors.toList());

                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //sku?????????????????????gulimall_sms->sms_sku_ladder/sms_sku_full_reduction/sms_member_price
                SkuReductionTo reductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,reductionTo);
                reductionTo.setSkuId(skuId);

                if (reductionTo.getFullCount() > 0 || reductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    R r1 = couponFeignService.saveSkuReduction(reductionTo);

                    //??????r???code???????????????????????????
//                    if (r1.getCode() != 0){
//                        log.error("????????????sku???????????????????????????");
//                    }
                }

            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByConditions(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        System.out.println(params);

        /**
         * status: 0
         * key:
         * catelogId: 225
         * page: 1
         * limit: 10
         * brandId: 9
         **/
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("id",key).or().like("spu_name",key);
            });
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }else {
            wrapper.eq("brand_id",1L);
        }


        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }else {
            wrapper.eq("catalog_id",225);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * ????????????
     **/
    @Override
    public void up(Long spuId) throws Exception{

        //????????????spuId???????????????sku??????
        List<SkuInfoEntity> skus =  skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //????????????sku?????????????????????????????????????????????
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrlistForspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        //??????????????????????????????????????????????????????
        List<Long> searchIds = attrService.selectSearchAttrs(attrIds);

        Set<Long> idSet = new HashSet<>(searchIds);

        List<SkuESModel.Attrs> attrsList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuESModel.Attrs attrs = new SkuESModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());


        //??????????????????????????????????????????????????????
        Map<Long, Boolean> stockMap = null;
        try {
            R hasStock = wareFeignService.hasStock(skuIdList);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>(){};

            stockMap = hasStock.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        }catch (Exception e){
            log.error("?????????????????????????????????{}",e);
        }


        //????????????sku?????????SkuESModel
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuESModel> upProducts = skus.stream().map(sku -> {
            //?????????????????????
            SkuESModel skuESModel = new SkuESModel();
            BeanUtils.copyProperties(sku,skuESModel);

            //skuPrice,skuImg
            skuESModel.setSkuPrice(sku.getPrice());
            skuESModel.setSkuImg(sku.getSkuDefaultImg());

            //hasStock,hotScore
            if (finalStockMap == null){
                skuESModel.setHasStock(false);
            }else {
                skuESModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            //????????????
            skuESModel.setHotScore(0L);

            //????????????????????????????????????
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            skuESModel.setBrandImg(brandEntity.getLogo());
            skuESModel.setBrandName(brandEntity.getName());

            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            skuESModel.setCatalogName(categoryEntity.getName());

            //??????????????????
            skuESModel.setAttrs(attrsList);

            return skuESModel;
        }).collect(Collectors.toList());


        //???????????????es?????????????????????????????????search??????
        R r = searchFeignService.productStatusUp(upProducts);
        //????????????r???code???0
        System.out.println(r.getCode());
        if (r.getCode() == 0){
            //?????????????????????????????????????????????
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else {
            //????????????
            //TODO ????????????????????????????????????????????????
        }

    }

    @Override
    public SpuInfoEntity spuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        Long spuId = byId.getSpuId();

        SpuInfoEntity spuInfoEntity = getById(spuId);

        return spuInfoEntity;
    }

}