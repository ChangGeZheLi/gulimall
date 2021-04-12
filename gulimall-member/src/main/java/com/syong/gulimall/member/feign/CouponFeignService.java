package com.syong.gulimall.member.feign;

import com.syong.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description: member服务调用coupon服务接口
 * 注意：@FeignClient中需要注册中心已注册的服务名
 *      @RequestMapping需要访问的全请求名称
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupons();
}
