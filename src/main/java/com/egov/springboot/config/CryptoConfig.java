package com.egov.springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:config/context-crypto.xml")
public class CryptoConfig {

}
