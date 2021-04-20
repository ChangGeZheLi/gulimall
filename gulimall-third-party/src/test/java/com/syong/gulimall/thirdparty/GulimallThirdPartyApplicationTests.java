package com.syong.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Resource
    private OSSClient ossClient;

    void contextLoads() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("D:\\FILES\\23d9fbb256ea5d4a.jpg");
        ossClient.putObject("gulimall-syong", "test2.jpg", inputStream);
        System.out.println("上传成功.....");
    }

}
