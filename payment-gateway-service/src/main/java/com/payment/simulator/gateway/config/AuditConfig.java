package com.payment.simulator.gateway.config;

import com.payment.simulator.common.audit.AuditLogAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditConfig {

    @Bean
    public AuditLogAspect auditLogAspect() {
        return new AuditLogAspect();
    }
}
