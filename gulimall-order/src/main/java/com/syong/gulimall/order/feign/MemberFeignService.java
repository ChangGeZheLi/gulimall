package com.syong.gulimall.order.feign;

import com.syong.gulimall.order.vo.MemberAddressVo;
import com.syong.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @Description: 远程调用会员服务
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memberId}/address")
    List<MemberAddressVo> getAddress(@PathVariable("memberId")Long memberId);

}
