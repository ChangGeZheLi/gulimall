package com.syong.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.syong.gulimall.thirdparty.component.SmsComponent;
import com.syong.gulimall.thirdparty.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallThirdPartyApplicationTests {

    @Resource
    private OSSClient ossClient;
    @Resource
    SmsComponent smsComponent;

    @Test
    public void contextLoads() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("D:\\FILES\\23d9fbb256ea5d4a.jpg");
        ossClient.putObject("gulimall-syong", "test2.jpg", inputStream);
        System.out.println("上传成功.....");
    }


    @Test
    public void contextLoads3() {
        smsComponent.sendSmsCode("17673606070","637894");
    }

    @Test
    public void contextLoads2() {

            String host = "https://edisim.market.alicloudapi.com";
            String path = "/comms/sms/sendmsg";
            String method = "POST";
            String appcode = "63b75d32cf50438fa48af473f4d98a73";
            Map<String, String> headers = new HashMap<String, String>();
            //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
            headers.put("Authorization", "APPCODE " + appcode);
            //根据API的要求，定义相对应的Content-Type
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            Map<String, String> querys = new HashMap<String, String>();
            Map<String, String> bodys = new HashMap<String, String>();
            bodys.put("callbackUrl", "http://test.dev.esandcloud.com");
            bodys.put("channel", "0");
            bodys.put("mobile", "17673606070");
            bodys.put("templateID", "20210604170620");
            bodys.put("templateParamSet", "1234");

            try {
                HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
                System.out.println(response.toString());
                //获取response的body
                //System.out.println(EntityUtils.toString(response.getEntity()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}
