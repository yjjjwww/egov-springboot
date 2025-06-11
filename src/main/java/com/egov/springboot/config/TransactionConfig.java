package com.egov.springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:config/context-transaction.xml")
public class TransactionConfig {

}
