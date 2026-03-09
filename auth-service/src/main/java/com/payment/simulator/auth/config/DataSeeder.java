package com.payment.simulator.auth.config;

import com.payment.simulator.auth.entity.User;
import com.payment.simulator.auth.repository.UserRepository;
import com.payment.simulator.common.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User("admin", passwordEncoder.encode("admin123"), UserRole.ADMIN);
                userRepository.save(admin);

                User merchant = new User("merchant1", passwordEncoder.encode("merchant123"), UserRole.MERCHANT);
                merchant.setMerchantId(1L);
                userRepository.save(merchant);

                log.info("Seeded default users: admin, merchant1");
            }
        };
    }
}
