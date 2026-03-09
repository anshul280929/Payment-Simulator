package com.payment.simulator.merchant.repository;

import com.payment.simulator.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findByApiKey(String apiKey);
    boolean existsByEmail(String email);
}
