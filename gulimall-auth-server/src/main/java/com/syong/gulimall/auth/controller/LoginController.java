package com.syong.gulimall.auth.controller;

import com.syong.common.utils.R;
import com.syong.gulimall.auth.feign.ThirdPartyFeign;
import com.syong.gulimall.auth.service.LoginService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @Description:
 */
@Controller
public class LoginController {

    @Resource
    private LoginService loginService;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("mobile") String mobile){

        loginService.sendCode(mobile);

        return R.ok();
    }
}
