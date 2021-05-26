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

    //TODO 高级篇完善
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //保存spu基本信息：pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        spuInfoEntity.setBrandId(9L);
        this.saveBaseSpuInfo(spuInfoEntity);

        //保存spu的描述图片：pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        //保存spu的图片集：pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);

        //保存spu的规格参数：pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            valueEntity.setId(spuInfoEntity.getId());
            //属性名需要通过id查询
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(attrEntity.getAttrName());

            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());

            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);

        //保存spu的积分信息：gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);

        //判断r的code，确定是否调用成功
//        if (r.getCode() != 0){
//            log.error("远程保存spu积分信息失败");
//        }

        //保存spu对应的sku信息
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0){
            skus.forEach(item->{

                //得到默认图片url
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

                //sku的基本信息：pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    skuImagesEntity.setImgUrl(img.getImgUrl());

                    return skuImagesEntity;
                }).filter(entity->{
                    //返回true就需要收集，否则不收集
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());

                //sku的图片：pms_sku_images
                //TODO 没有图片路径的无需保存
                skuImagesService.saveBatch(imagesEntities);

                //sku的销售属性信息：pms_sku_sale_attr_value
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = item.getAttr().stream().map(a -> {
                    SkuSaleAttrValueEntity saleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, saleAttrValueEntity);
                    saleAttrValueEntity.setSkuId(skuId);

                    return saleAttrValueEntity;
                }).collect(Collectors.toList());

                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //sku的优惠、满减：gulimall_sms->sms_sku_ladder/sms_sku_full_reduction/sms_member_price
                SkuReductionTo reductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,reductionTo);
                reductionTo.setSkuId(skuId);

                if (reductionTo.getFullCount() > 0 || reductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    R r1 = couponFeignService.saveSkuReduction(reductionTo);

                    //判断r的code，确定是否调用成功
//                    if (r1.getCode() != 0){
//                        log.error("远程保存sku优惠、满减信息失败");
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
     * 商品上架
     **/
    @Override
    public void up(Long spuId) throws Exception{

        //查出当前spuId对应的所有sku信息
        List<SkuInfoEntity> skus =  skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //查询当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrlistForspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        //在指定的所有属性集合中，挑出检索属性
        List<Long> searchIds = attrService.selectSearchAttrs(attrIds);

        Set<Long> idSet = new HashSet<>(searchIds);

        List<SkuESModel.Attrs> attrsList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuESModel.Attrs attrs = new SkuESModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());


        //发送远程调用，库存系统查询是否有库存
        Map<Long, Boolean> stockMap = null;
        try {
            R hasStock = wareFeignService.hasStock(skuIdList);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>(){};

            stockMap = hasStock.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        }catch (Exception e){
            log.error("库存远程调用异常，原因{}",e);
        }


        //封装每个sku信息成SkuESModel
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuESModel> upProducts = skus.stream().map(sku -> {
            //组装需要的数据
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

            //热度评分
            skuESModel.setHotScore(0L);

            //查询品牌和分类名字和信息
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            skuESModel.setBrandImg(brandEntity.getLogo());
            skuESModel.setBrandName(brandEntity.getName());

            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            skuESModel.setCatalogName(categoryEntity.getName());

            //设置检索属性
            skuESModel.setAttrs(attrsList);

            return skuESModel;
        }).collect(Collectors.toList());


        //发送数据给es进行保存，需要远程调用search服务
        R r = searchFeignService.productStatusUp(upProducts);
        //调用成功r的code为0
        System.out.println(r.getCode());
        if (r.getCode() == 0){
            //调用成功，需要将状态更改为上架
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else {
            //调用失败
            //TODO 重复调用？接口幂等性；重试机制？
        }

    }

}