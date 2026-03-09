package com.payment.simulator.gateway.config;

import com.payment.simulator.common.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptionConfig {

    @Value("${encryption.secret-key}")
    private String secretKey;

    @Bean
    public EncryptionUtil encryptionUtil() {
        return new EncryptionUtil(secretKey);
    }
}
