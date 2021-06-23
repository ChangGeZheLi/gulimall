package com.syong.gulimall.secondkill.controller;

import com.syong.common.utils.R;
import com.syong.gulimall.secondkill.service.SeckillService;
import com.syong.gulimall.secondkill.to.SeckillSkuRedisTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:
 */
@RestController
public class SeckillController {

    @Resource
    private SeckillService seckillService;

    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisTo> tos = seckillService.getCurrentSeckillSkus();

        System.out.println(tos);

        return R.ok().setData(tos);
    }

    /**
     * 返回当前sku是否参与秒杀优惠
     **/
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId){
        SeckillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);

        return R.ok().setData(to);
    }

    @GetMapping("/kill")
    public R secKill(@RequestParam("killId")String killId,
                     @RequestParam("key")String key,
                     @RequestParam("num") Integer num){


        return R.ok();
    }
}
