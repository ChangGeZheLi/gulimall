package com.syong.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description: 解决feign远程调用会丢失请求头数据问题，需要自定义feign的拦截器，在源码中，feign会挨个调用容器中的拦截器，构建请求
 */
@Configuration
public class GuliFeignConfig{

    @Bean
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                //RequestContextHolder可以拿到刚进来的请求
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                //老请求数据
                if (attributes!=null){
                    HttpServletRequest request = attributes.getRequest();
                    if (request != null){
                        //同步请求头数据
                        String cookie = request.getHeader("Cookie");
                        //给新请求同步老请求的cookie
                        template.header("Cookie", cookie);
                    }
                }
            }
        };
    }
}
