package com.syong.gulimall.auth.controller;

import com.syong.common.utils.HttpUtils;
import com.syong.common.utils.R;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 处理社交登录请求
 */
@Controller
public class OAuth2Controller {

    /**
     * 处理社交登录成功回调
     **/
    @GetMapping("/oauth/gitee/success")
    public String gitee(@RequestParam("code") String code) throws Exception {

        Map<String,String> map = new HashMap<>();
        map.put("grant_type","authorization_code");
        map.put("code",code);
        map.put("client_id","516e698a6d843bf16260324036531fe48fa879242da2389ae4e259fe1f144570");
        map.put("redirect_uri","http://auth.gulimall.com/oauth/gitee/success");
        map.put("client_secret","f717f80d11ba2b4dc73262ea3c1ba0ad6bf9531f178c1ae4c106eee72d0de7b6");

        //根据授权码，换取access_token
        HttpResponse response = HttpUtils.doPost("gitee.com", "/oauth/token", "post", null, null, map);

        //登录成功就跳回首页
        return "redirect:http://gulimall.com";
    }
}
