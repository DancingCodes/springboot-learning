package com.example.springbootlearning.config;

import com.example.springbootlearning.interceptor.LogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogInterceptor())
                .addPathPatterns("/**")           // 拦截所有路径
                .excludePathPatterns("/doc.html", "/v3/api-docs/**", "/webjars/**"); // 排除文档
    }
}
