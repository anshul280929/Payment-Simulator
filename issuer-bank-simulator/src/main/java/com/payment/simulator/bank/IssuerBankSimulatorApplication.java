package com.payment.simulator.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class IssuerBankSimulatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(IssuerBankSimulatorApplication.class, args);
    }
}
