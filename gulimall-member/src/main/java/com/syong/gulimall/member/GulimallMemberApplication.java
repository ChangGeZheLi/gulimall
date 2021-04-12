package com.syong.gulimall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 使用openFeign远程调用
 * 1、导入openFeign依赖
 * 2、编写远程调用接口
 *      声明接口的每一个方法需要调用哪个微服务的哪个请求
 * 3、使用@EnableFeignClients开启远程调用功能
 **/

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.syong.gulimall.member.feign")
public class GulimallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallMemberApplication.class, args);
    }

}
