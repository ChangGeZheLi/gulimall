package com.syong.gulimall.secondkill.config;

import com.syong.gulimall.secondkill.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Description: 配置自定义拦截器到容器
 */
@Configuration
public class SeckillWebConfig implements WebMvcConfigurer {

//    @Resource
//    LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/**");
    }
}
