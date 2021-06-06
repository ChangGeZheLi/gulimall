package com.syong.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.syong.common.utils.HttpUtils;
import com.syong.common.utils.R;
import com.syong.common.vo.MemberEntity;
import com.syong.gulimall.auth.feign.MemberFeignService;
import com.syong.gulimall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 处理社交登录请求
 */
@Slf4j
@Controller
public class OAuth2Controller {

    @Resource
    private MemberFeignService memberFeignService;

    /**
     * 处理社交登录成功回调
     **/
    @GetMapping("/oauth/gitee/success")
    public String gitee(@RequestParam("code") String code, HttpSession session) throws Exception {

        Map<String, String> headers = new HashMap<>();

        Map<String,String> map = new HashMap<>();
        map.put("grant_type","authorization_code");
        map.put("code",code);
        map.put("client_id","516e698a6d843bf16260324036531fe48fa879242da2389ae4e259fe1f144570");
        map.put("redirect_uri","http://auth.gulimall.com/oauth/gitee/success");
        map.put("client_secret","f717f80d11ba2b4dc73262ea3c1ba0ad6bf9531f178c1ae4c106eee72d0de7b6");

        //根据授权码，换取access_token
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", headers,null,map);

        //处理
        if (response.getStatusLine().getStatusCode() == 200){
            //换取成功
            String json = EntityUtils.toString(response.getEntity());
            //将换回的json转为指定对象
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            //根据access_token再次访问gitee openAPI 拿到用户的其他数据
            Map<String, String> querys = new HashMap<>();
            querys.put("access_token",socialUser.getAccess_token());
            HttpResponse doGet = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", headers, querys);
            if (doGet.getStatusLine().getStatusCode() == 200){
                String user = EntityUtils.toString(doGet.getEntity());
                //将拿到的数据转为JSONObject，然后将数据封装
                JSONObject jsonObject = JSON.parseObject(user);

                socialUser.setUid(jsonObject.getLong("id"));
                socialUser.setName(jsonObject.getString("name"));
                socialUser.setCreateTime(jsonObject.getDate("created_at"));
            }

            //如果用户第一次进网站，自动注册
            //判断登录或者注册,远程调用会员服务进行操作
            R r = memberFeignService.oauthLogin(socialUser);
            if (r.getCode() == 0){
                // 登录成功
                MemberEntity data = r.getData("data", new TypeReference<MemberEntity>() {});
                log.info("登录成功，用户信息：{}",data.toString());

                //将返回的个人信息存入session
                session.setAttribute("loginUser",data);


                //返回首页
                return "redirect:http://gulimall.com";
            }else {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }else {
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
