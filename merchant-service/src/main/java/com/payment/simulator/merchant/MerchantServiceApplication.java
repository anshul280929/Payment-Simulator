package com.payment.simulator.merchant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class MerchantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MerchantServiceApplication.class, args);
    }

    @Bean
    Runnable logDatasourceConfig(
            @Value("${spring.datasource.url:}") String datasourceUrl,
            @Value("${spring.datasource.username:}") String datasourceUsername
    ) {
        return () -> System.out.println(
                "[merchant-service] Effective spring.datasource.url=" + datasourceUrl
                        + ", username=" + datasourceUsername
        );
    }
}
