package com.example.demo.config;

import com.example.demo.utils.LoginInterceptor;
import com.example.demo.utils.RedisCache;
import com.example.demo.utils.RefreshTokenInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    RedisCache redisCache;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println("进入拦截器。。。");
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "/user/code",
                        "/user/login",
                        "/blog/hot",
                        "/shop/**",
                        "/shop-type/**",
                        "/upload/**",
                        "/voucher/**"
                        ).order(1);

        registry.addInterceptor(new RefreshTokenInterceptor(redisCache))
                .addPathPatterns("/**").order(0);

    }




}
