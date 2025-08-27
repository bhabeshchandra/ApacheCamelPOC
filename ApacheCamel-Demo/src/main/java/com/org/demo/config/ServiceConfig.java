package com.org.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Value("${app.service.type}")
    private String serviceType;

    public String getServiceType() {
        return serviceType;
    }
}