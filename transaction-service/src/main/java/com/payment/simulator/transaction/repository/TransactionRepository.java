package com.payment.simulator.transaction.repository;

import com.payment.simulator.common.enums.TransactionStatus;
import com.payment.simulator.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
    List<Transaction> findByMerchantId(Long merchantId);
    List<Transaction> findByStatus(TransactionStatus status);
    List<Transaction> findByMerchantIdAndStatus(Long merchantId, TransactionStatus status);
    List<Transaction> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
