package com.syong.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.product.dao.SkuInfoDao;
import com.syong.gulimall.product.entity.SkuInfoEntity;
import com.syong.gulimall.product.service.SkuInfoService;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

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
         * key: '华为',//检索关键字
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
                //考虑到前端传默认值的情况，也就是传值max为0情况
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

}