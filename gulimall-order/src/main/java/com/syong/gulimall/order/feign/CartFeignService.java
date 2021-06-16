package com.syong.gulimall.order.feign;

import com.syong.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @Description: 远程调用购物车服务
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {
    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();
}
