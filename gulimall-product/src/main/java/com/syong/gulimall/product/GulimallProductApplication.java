package com.syong.gulimall.product;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

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
 *
 *  JSR303
 *  1、给bean添加校验注解：javax.validation.constraints，并定义自己的message提示信息(可以直接抽取成一个枚举类)
 *  2、在controller中需要进行校验的地方添加校验注解：@Valid @RequestBody BrandEntity brand
 *  3、在校验的controller中添加参数：（@Valid @RequestBody BrandEntity brand BindResult result）
 *  4、分组校验
 *      1、将实体类中的校验进行分组，UpdateGroup，AddGroup需要定义为空接口
 *        @NotNull(message = "修改必须指定品牌id",groups = {UpdateGroup.class})
 *        @Null(message = "新增不能指定品牌id",groups = {AddGroup.class})
 *      2、在controller中更改校验注解：
 *          (@Validated({AddGroup.class}) @RequestBody BrandEntity brand)
 *      3、默认没有指定分组的校验注解，在分组校验情况下不生效，只会在@Validated没有指定分组情况下生效
 *  5、自定义校验
 *      1、编写一个自定义的校验注解，指定默认信息位置： String message() default "{com.syong.common.valid.ListValue.message}";
 *      2、编写一个自定义校验器
 *      3、关联自定义注解和校验器,可以指定多个校验器
 *          @Constraint(validatedBy = {ListValueConstraintValidator.class})
 *
 **/
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.syong.gulimall.product.dao")
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
