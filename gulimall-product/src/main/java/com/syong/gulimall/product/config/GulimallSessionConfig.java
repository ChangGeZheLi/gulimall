package com.syong.gulimall.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @Description: 自定义springSession的作用域以及序列化机制
 */
@Configuration
public class GulimallSessionConfig {

    /**
     * 自定义CookieSerializer，解决父子域之间的session共享问题
     **/
    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setCookieName("GULISESSION");
        cookieSerializer.setDomainName("gulimall.com");

        return cookieSerializer;
    }

    /**
     * session使用json序列化机制
     **/
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(){
        return new  GenericJackson2JsonRedisSerializer();
    }

}
