package com.egov.springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.egov.springboot.com.cmm.interceptor.AuthenticInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/uat/uia/egovLoginUsr.do",
                        "/uat/uia/actionLogin.do",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/resources/**"
                );
    }
}
