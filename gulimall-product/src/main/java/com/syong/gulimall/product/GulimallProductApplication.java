package com.syong.gulimall.product;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *  整合Mybatis-plus
 *      1)、导入依赖
 *      <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.4.2</version>
 *         </dependency>
 *        2)、配置
 *          1、配置数据源
 *              导入对应版本的数据库驱动
 *              在application.yml中配置数据源信息
 *          2、配置mybatis-plus
 *              @MapperScan
 **/
@SpringBootApplication
@MapperScan("com.syong.gulimall.product.dao")
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
