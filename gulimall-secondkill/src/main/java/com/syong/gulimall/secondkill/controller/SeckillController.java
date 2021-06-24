package com.syong.gulimall.secondkill.controller;

import com.syong.common.utils.R;
import com.syong.gulimall.secondkill.service.SeckillService;
import com.syong.gulimall.secondkill.to.SeckillSkuRedisTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:
 */
@Controller
public class SeckillController {

    @Resource
    private SeckillService seckillService;

    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisTo> tos = seckillService.getCurrentSeckillSkus();

        System.out.println(tos);

        return R.ok().setData(tos);
    }

    /**
     * 返回当前sku是否参与秒杀优惠
     **/
    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId){
        SeckillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);

        return R.ok().setData(to);
    }

    @GetMapping("/kill")
    public String secKill(@RequestParam("killId")String killId,
                          @RequestParam("key")String key,
                          @RequestParam("num") Integer num, Model model){

        String orderSn = seckillService.kill(killId,key,num);

        model.addAttribute("orderSn",orderSn);
        return "success";
    }
}
