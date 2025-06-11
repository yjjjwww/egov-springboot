package com.egov.springboot.config;


import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.egov.springboot.com.cmm.EgovMessageSource;

//@Configuration
public class EgovMessageConfig {

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
            "classpath:/egovframework/message/com/message-common",
            "classpath:/org/egovframe/rte/fdl/idgnr/messages/idgnr",
            "classpath:/org/egovframe/rte/fdl/property/messages/properties"
        );
        messageSource.setCacheSeconds(60);
        return messageSource;
    }

    @Bean
    public EgovMessageSource egovMessageSource(MessageSource messageSource) {
        EgovMessageSource egovMessageSource = new EgovMessageSource();
        egovMessageSource.setReloadableResourceBundleMessageSource((ReloadableResourceBundleMessageSource) messageSource);
        return egovMessageSource;
    }
}