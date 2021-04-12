package com.syong.gulimall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 使用nacos作为配置中心
 * 1、导入nacas config依赖
 *         <dependency>
 *             <groupId>com.alibaba.cloud</groupId>
 *             <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
 *         </dependency>
 *  2、添加bootstrap.properties配置文件，并且配置一下信息
 *         spring.application.name=gulimall-coupon
 *         spring.cloud.nacos.config.server-addr=127.0.0.1:8848
 *  3、在nacos中添加数据集（DataId），默认规则：应用名.properties，并在其中添加所需配置
 *  4、在需要动态刷新配置使用
 *          @RefreshScope： 动态获取并且刷新配置
 *          @Value("${配置项名}"： 获取配置中的值
 *
 * 细节：
 *  1、命名空间：配置隔离
 *      默认：public(保留空间)；默认新增的所有配置都在public空间
 *      开发、测试、生产：利用命名空间来做环境隔离
 *      在bootstrap.properties中配置需要使用哪个命名空间下的配置
 *           spring.cloud.nacos.config.namespace
 *  2、配置集：所有配置的集合
 *  3、配置集ID：
 *      DataId：类似文件名 gulimall-coupon.properties
 *  4、配置分组
 *      默认所有的配置集都属于DEFAULT_GROUP
 *
 * 每个微服务创建自己的命名空间，使用配置分组区分环境：dev，test，prod
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class GulimallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCouponApplication.class, args);
    }

}
