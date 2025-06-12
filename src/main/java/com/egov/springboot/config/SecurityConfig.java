package com.egov.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // CSRF 보호 비활성화
            .cors(cors -> cors.disable())  // CORS 비활성화
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/**").permitAll()  // 모든 URL 허용
            )
            .formLogin(form -> form.disable())  // Spring Security 기본 로그인 폼 비활성화
            .httpBasic(basic -> basic.disable())  // HTTP Basic 인증 비활성화
            .anonymous(anonymous -> anonymous.disable())  // 익명 사용자 비활성화
            .sessionManagement(session -> session.disable());  // 세션 관리 비활성화
        
        return http.build();
    }
}
