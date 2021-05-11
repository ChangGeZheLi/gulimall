package com.syong.gulimall.ware.feign;

import com.syong.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description: 远程调用product服务
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    /**
     * 1)feign调用过网关
     *      FeignClient("gulimall-gateway")
     *      /api/product/skuinfo/info/{skuId}
     *  2)服务不过网关
     *      FeignClient("gulimall-product")
     *      /product/skuinfo/info/{skuId}
     **/
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
