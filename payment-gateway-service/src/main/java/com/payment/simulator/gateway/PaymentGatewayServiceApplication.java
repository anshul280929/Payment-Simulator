package com.payment.simulator.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PaymentGatewayServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentGatewayServiceApplication.class, args);
    }
}
