package com.syong.gulimall.cart.config;

import com.syong.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Description: 配置拦截器，以及拦截路径
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //配置拦截器以及拦截路径
        registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
    }
}
