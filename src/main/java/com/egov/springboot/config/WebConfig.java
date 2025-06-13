package com.egov.springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 설정을 위한 클래스
 * CORS 설정 등을 처리한다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * CORS 설정
     * 프론트엔드 애플리케이션(localhost:5173)에서 백엔드 API 호출을 허용한다.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}