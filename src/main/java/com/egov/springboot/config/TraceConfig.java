package com.egov.springboot.config;


import org.egovframe.rte.fdl.cmmn.trace.manager.DefaultTraceHandleManager;
import org.egovframe.rte.fdl.cmmn.trace.manager.TraceHandlerService;
import org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace;
import org.egovframe.rte.fdl.cmmn.trace.handler.TraceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import com.egov.springboot.com.cmm.EgovComTraceHandler;

import antlr.collections.List;

import java.util.Arrays;
import java.util.Collections;

//@Configuration
public class TraceConfig {

    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }

    @Bean
    public TraceHandler defaultTraceHandler() {
        return new EgovComTraceHandler();
    }

    @Bean
    public DefaultTraceHandleManager traceHandlerService() {
        DefaultTraceHandleManager manager = new DefaultTraceHandleManager();
        manager.setReqExpMatcher(antPathMatcher());
        manager.setPatterns(new String[] { "*" });
        manager.setHandlers(new TraceHandler[] { defaultTraceHandler() }); // 배열로 넘김
        return manager;
    }
    
    @Bean
    public LeaveaTrace leaveaTrace() {
        LeaveaTrace leaveaTrace = new LeaveaTrace();
        leaveaTrace.setTraceHandlerServices(new TraceHandlerService[] { traceHandlerService() });
        return leaveaTrace;
    }
}