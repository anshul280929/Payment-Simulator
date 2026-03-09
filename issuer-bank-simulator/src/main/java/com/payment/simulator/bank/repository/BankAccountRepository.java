package com.payment.simulator.bank.repository;

import com.payment.simulator.bank.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByCardNumber(String cardNumber);
}
