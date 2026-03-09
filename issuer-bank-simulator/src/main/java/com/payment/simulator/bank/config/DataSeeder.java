package com.payment.simulator.bank.config;

import com.payment.simulator.bank.entity.BankAccount;
import com.payment.simulator.bank.repository.BankAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedBankAccounts(BankAccountRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                // Always-approved test card
                createAccount(repository, "4111111111111234", "Test User Approved",
                        "12", "2030", "123", new BigDecimal("50000.00"));

                // Insufficient funds card
                createAccount(repository, "4111111111110000", "Test User Insufficient",
                        "12", "2030", "123", new BigDecimal("0.00"));

                // Expired card
                createAccount(repository, "4111111111111111", "Test User Expired",
                        "01", "2020", "123", new BigDecimal("10000.00"));

                // Timeout card
                createAccount(repository, "4111111111119999", "Test User Timeout",
                        "12", "2030", "123", new BigDecimal("10000.00"));

                // Regular test cards
                createAccount(repository, "4532015112830366", "John Doe",
                        "06", "2028", "456", new BigDecimal("25000.00"));
                createAccount(repository, "5425233430109903", "Jane Smith",
                        "09", "2029", "789", new BigDecimal("15000.00"));

                log.info("Seeded 6 test bank accounts");
            }
        };
    }

    private void createAccount(BankAccountRepository repo, String cardNumber,
                               String name, String expMonth, String expYear,
                               String cvv, BigDecimal balance) {
        BankAccount account = new BankAccount();
        account.setCardNumber(cardNumber);
        account.setCardholderName(name);
        account.setExpiryMonth(expMonth);
        account.setExpiryYear(expYear);
        account.setCvv(cvv);
        account.setBalance(balance);
        repo.save(account);
    }
}
