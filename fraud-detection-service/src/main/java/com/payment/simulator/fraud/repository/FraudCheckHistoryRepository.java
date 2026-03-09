package com.payment.simulator.fraud.repository;

import com.payment.simulator.fraud.entity.FraudCheckHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FraudCheckHistoryRepository extends JpaRepository<FraudCheckHistory, Long> {
    List<FraudCheckHistory> findByCardTokenAndCheckedAtAfter(String cardToken, LocalDateTime after);
    long countByCardTokenAndCheckedAtAfter(String cardToken, LocalDateTime after);
}
