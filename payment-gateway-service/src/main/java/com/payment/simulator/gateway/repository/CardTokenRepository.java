package com.payment.simulator.gateway.repository;

import com.payment.simulator.gateway.entity.CardToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardTokenRepository extends JpaRepository<CardToken, Long> {
    Optional<CardToken> findByToken(String token);
}
