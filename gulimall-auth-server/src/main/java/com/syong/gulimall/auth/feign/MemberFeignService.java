package com.syong.gulimall.auth.feign;

import com.syong.common.utils.R;
import com.syong.gulimall.auth.vo.SocialUser;
import com.syong.gulimall.auth.vo.UserLoginVo;
import com.syong.gulimall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description: 远程调用会员服务
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    /**
     * 社交登录
     **/
    @PostMapping("/member/member/oauth/login")
    R oauthLogin(@RequestBody SocialUser user);
}
