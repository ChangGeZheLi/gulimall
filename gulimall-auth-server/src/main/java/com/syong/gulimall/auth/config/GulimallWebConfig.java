package com.syong.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Description: 实现不用写controller，将页面请求映射到指定位置
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    /**
     * 视图映射
     * addViewController("/login.html").setViewName("login")
     * 分别对应controller中的 @GetMapping("/login.html")和return "login"
     **/
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/register.html").setViewName("register");
    }
}
