package com.syong.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.syong.gulimall.product.entity.AttrEntity;
import com.syong.gulimall.product.service.AttrAttrgroupRelationService;
import com.syong.gulimall.product.service.AttrService;
import com.syong.gulimall.product.service.CategoryService;
import com.syong.gulimall.product.vo.AttrGroupRelationVo;
import com.syong.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.syong.gulimall.product.entity.AttrGroupEntity;
import com.syong.gulimall.product.service.AttrGroupService;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.R;

import javax.annotation.Resource;


/**
 * 属性分组
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 12:21:40
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @Resource
    private AttrService attrService;
    @Resource
    private AttrAttrgroupRelationService relationService;

    /**
     * /product/attrgroup/{catelogId}/withattr
     **/
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGourpWithAttrs(@PathVariable("catelogId") Long catelogId){
        List<AttrGroupWithAttrsVo> vos =  attrGroupService.getAttrGourpWithAttrsByCatelogId(catelogId);

        return R.ok().put("data",vos);
    }

    /**
     * /product/attrgroup/attr/relation
     **/
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos){
        relationService.saveBatch(vos);

        return R.ok();
    }

    /**
     * /product/attrgroup/{attrgroupId}/noattr/relation
     **/
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                            @RequestParam Map<String,Object> params){
        PageUtils page = attrGroupService.getNoRelationAttr(params,attrgroupId);
        return R.ok().put("page",page);
    }

    /**
     * /product/attrgroup/attr/relation/delete
     **/
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos ){
        System.out.println(Arrays.asList(vos));
        attrService.deleteRelation(vos);
        return R.ok();
    }

    /**
     * /product/attrgroup/{attrgroupId}/attr/relation
     **/
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> entities =  attrService.getRelationAttr(attrgroupId);

        return R.ok().put("data",entities);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catalogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("catalogId") Long catelogId){
//        PageUtils page = attrGroupService.queryPage(params);

        PageUtils page = attrGroupService.queryPage(params,catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     * 返回三级分类id的完整路径
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long catalogId = attrGroup.getCatelogId();
        Long[] catalogPath = categoryService.getCatalogPathById(catalogId);

        attrGroup.setCatelogPath(catalogPath);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
