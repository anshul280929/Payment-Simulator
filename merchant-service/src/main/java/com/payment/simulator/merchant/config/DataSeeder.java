package com.payment.simulator.merchant.config;

import com.payment.simulator.merchant.entity.Merchant;
import com.payment.simulator.merchant.repository.MerchantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedMerchants(MerchantRepository merchantRepository) {
        return args -> {
            if (merchantRepository.count() == 0) {
                Merchant merchant = new Merchant();
                merchant.setName("Demo E-Commerce Store");
                merchant.setEmail("demo@store.com");
                merchant.setApiKey("pk_test_demo_merchant_key_001");
                merchant.setWebhookUrl("https://demo-store.example.com/webhooks/payment");
                merchantRepository.save(merchant);

                Merchant merchant2 = new Merchant();
                merchant2.setName("Test Marketplace");
                merchant2.setEmail("test@marketplace.com");
                merchant2.setApiKey("pk_test_marketplace_key_002");
                merchantRepository.save(merchant2);

                log.info("Seeded 2 demo merchants");
            }
        };
    }
}
