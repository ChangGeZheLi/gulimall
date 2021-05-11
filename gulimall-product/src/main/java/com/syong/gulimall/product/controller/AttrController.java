package com.syong.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.syong.gulimall.product.entity.ProductAttrValueEntity;
import com.syong.gulimall.product.service.ProductAttrValueService;
import com.syong.gulimall.product.vo.AttrGroupRelationVo;
import com.syong.gulimall.product.vo.AttrResponseVo;
import com.syong.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.syong.gulimall.product.entity.AttrEntity;
import com.syong.gulimall.product.service.AttrService;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.R;

import javax.annotation.Resource;


/**
 * 商品属性
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 12:21:40
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Resource
    private ProductAttrValueService productAttrValueService;

    /**
     * /product/attr/base/listforspu/{spuId}
     * 获取spu规格
     **/
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrlistForspu(@PathVariable("spuId") Long spuId){

        List<ProductAttrValueEntity> entities =  productAttrValueService.baseAttrlistForspu(spuId);

        return R.ok().put("data",entities);
    }

    /**
     * /product/attr/base/list/{catelogId}
     * /product/attr/sale/list/{catelogId}
     **/
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String,Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String type){

        PageUtils page = attrService.queryBaseAttrPage(params,catelogId,type);
        return R.ok().put("page",page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
//		AttrEntity attr = attrService.getById(attrId);
        AttrResponseVo attrResponseVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attrResponseVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改商品规格
     * /product/attr/update/{spuId}
     */
    @PostMapping("/update/{spuId}")
    //@RequiresPermissions("product:attr:update")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities){
        productAttrValueService.updateSpuAttr(spuId,entities);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrResponseVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
