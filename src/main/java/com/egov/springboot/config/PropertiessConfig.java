package com.egov.springboot.config;

import java.util.HashMap;
import java.util.Map;

import org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class PropertiessConfig {

    @Bean(destroyMethod = "destroy", name = "propertiesService")
    public EgovPropertyServiceImpl propertiesService() {
        EgovPropertyServiceImpl propertiesService = new EgovPropertyServiceImpl();
        
        Map<String, Object> props = new HashMap<>();
        props.put("pageUnit", "10");
        props.put("pageSize", "10");
        props.put("posblAtchFileSize", "5242880");
        props.put("Globals.fileStorePath", "/user/file/sht/");
        props.put("Globals.addedOptions", "false");
        
        propertiesService.setProperties(props);
        
        return propertiesService;
    }
}