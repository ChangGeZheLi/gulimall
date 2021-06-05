package com.syong.gulimall.thirdparty.controller;

import com.syong.common.utils.R;
import com.syong.gulimall.thirdparty.component.SmsComponent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description:
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {

    @Resource
    private SmsComponent smsComponent;

    /**
     * 提供给别的服务进行调用的，而不是页面直接映射到该controller中
     **/
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("mobile") String mobile,@RequestParam("code") String code){
        smsComponent.sendSmsCode(mobile,code);
        return R.ok();
    }
}
