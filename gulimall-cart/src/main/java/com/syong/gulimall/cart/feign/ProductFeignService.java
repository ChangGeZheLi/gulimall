package com.syong.gulimall.cart.feign;

import com.syong.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: 远程调用商品服务
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

    /**
     * 根据skuId查询销售属性值
     **/
    @GetMapping("/product/skusaleattrvalue/stringlist/{skuId}")
    List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skuinfo/{skuId}/price")
    R getPrice(@PathVariable("skuId")Long skuId);
}
