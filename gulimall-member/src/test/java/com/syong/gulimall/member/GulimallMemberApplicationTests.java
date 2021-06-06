package com.syong.gulimall.member;

import com.alibaba.fastjson.JSON;
import com.syong.common.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallMemberApplicationTests {

    @Test
    public void contextLoads() throws Exception {
        Map<String, String> querys = new HashMap<>();
        Map<String, String> headers= new HashMap<>();
        querys.put("access_token","e434b435e0ad50c0f913e47d20b8ba4d");
        HttpResponse doGet = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", headers, querys);
        if (doGet.getStatusLine().getStatusCode() == 200){
            String user = EntityUtils.toString(doGet.getEntity());


            System.out.println(user);
//                socialUser.setUid();
        }
    }

}
