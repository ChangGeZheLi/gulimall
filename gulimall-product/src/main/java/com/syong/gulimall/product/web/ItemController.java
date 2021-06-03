package com.syong.gulimall.product.web;

import com.syong.gulimall.product.service.SkuInfoService;
import com.syong.gulimall.product.vo.foregroundVo.SkuItemVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;

/**
 * @Description:
 */
@Controller
public class ItemController {

    @Resource
    private SkuInfoService skuInfoService;

    /**
     * 展示当前sku的详情
     **/
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model){

        SkuItemVo vo = skuInfoService.item(skuId);

        model.addAttribute("item",vo);

        return "item";
    }
}
