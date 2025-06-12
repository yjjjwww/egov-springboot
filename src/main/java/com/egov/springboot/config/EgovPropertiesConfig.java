package com.egov.springboot.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.egov.springboot.com.cmm.service.EgovProperties;

@Configuration
public class EgovPropertiesConfig {

    @Bean
    public EgovProperties egovProperties(Environment environment, ApplicationContext context) {
        return new EgovProperties(environment, context);
    }
}
