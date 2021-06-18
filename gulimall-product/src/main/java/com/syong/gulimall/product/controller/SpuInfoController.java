package com.syong.gulimall.product.controller;

import java.util.Arrays;
import java.util.Map;

import com.syong.gulimall.product.vo.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.syong.gulimall.product.entity.SpuInfoEntity;
import com.syong.gulimall.product.service.SpuInfoService;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.R;



/**
 * spu信息
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 12:21:40
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    @GetMapping("/skuId/{id}")
    public R spuInfoBySkuId(@PathVariable("id")Long skuId){
        SpuInfoEntity spuInfoEntity = spuInfoService.spuInfoBySkuId(skuId);

        return R.ok().setData(spuInfoEntity);
    }

    /**
     * /product/spuinfo/{spuId}/up
     * 商品上架
     **/
    @PostMapping("/{spuId}/up")
    public R spuUp(@PathVariable("spuId") Long spuId) throws Exception {
        spuInfoService.up(spuId);

        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.queryPageByConditions(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:spuinfo:info")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存
     * /product/spuinfo/save
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:spuinfo:save")
    public R save(@RequestBody SpuSaveVo vo){
		spuInfoService.saveSpuInfo(vo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:spuinfo:delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
