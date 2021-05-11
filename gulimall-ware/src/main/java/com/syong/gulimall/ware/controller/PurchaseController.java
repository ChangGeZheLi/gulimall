package com.syong.gulimall.ware.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.syong.gulimall.ware.vo.MergeVo;
import com.syong.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.syong.gulimall.ware.entity.PurchaseEntity;
import com.syong.gulimall.ware.service.PurchaseService;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.R;



/**
 * 采购信息
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 16:34:14
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * /ware/purchase/done
     * 完成采购
     **/
    @PostMapping("done")
    public R done(@RequestBody PurchaseDoneVo vo){
        purchaseService.done(vo);

        return R.ok();
    }

    /**
     * /ware/purchase/received
     * 领取采购单
     **/
    @PostMapping("received")
    public R received(@RequestBody List<Long> ids){
        purchaseService.received(ids);

        return R.ok();
    }

    /**
     * /ware/purchase/merge
     * 合并采购需求
     **/
    @PostMapping("/merge")
    //@RequiresPermissions("ware:purchase:list")
    public R merge(@RequestBody MergeVo mergeVo){
        purchaseService.mergePurchase(mergeVo);

        return R.ok();
    }

    /**
     * /ware/purchase/unreceive/list
     * 查询未领取的采购单
     **/
    @GetMapping("/unreceive/list")
    //@RequiresPermissions("ware:purchase:list")
    public R unreceiveList(@RequestParam Map<String,Object> params){
        PageUtils page = purchaseService.queryUnreceivePurchase(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
        purchase.setCreateTime(new Date());
        purchase.setUpdateTime(new Date());

		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
