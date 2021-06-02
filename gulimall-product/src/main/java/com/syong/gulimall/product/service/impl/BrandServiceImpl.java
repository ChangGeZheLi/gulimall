package com.syong.gulimall.product.service.impl;

import com.syong.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.product.dao.BrandDao;
import com.syong.gulimall.product.entity.BrandEntity;
import com.syong.gulimall.product.service.BrandService;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        //获取key，进行模糊检索
        String  key = (String) params.get("key");
        //如果有传递key值进行模糊检索
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)){
            queryWrapper.eq("brand_id",key).or().like("name",key);
        }

        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 为了减少数据库性能损耗，更新关联表中的信息在后台完成
     **/
    @Override
    public void updateDetail(BrandEntity brand) {
        //保证所有冗余字段的数据一致性
        this.updateById(brand);
        if (!StringUtils.isEmpty(brand.getName())){
            //需要更新品牌名，则同步更新关联表中的品牌名
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());

            //TODO 更新其他关联
        }
    }

    @Override
    public List<BrandEntity> getBrandsByIds(List<Long> branIds) {
        List<BrandEntity> brandEntities = baseMapper.selectList(new QueryWrapper<BrandEntity>().in("brand_id", branIds));
        return brandEntities;
    }

}