package com.payment.simulator.gateway.repository;

import com.payment.simulator.common.enums.TransactionStatus;
import com.payment.simulator.gateway.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    Optional<PaymentRecord> findByTransactionId(String transactionId);
    List<PaymentRecord> findByMerchantId(Long merchantId);
    List<PaymentRecord> findByStatus(TransactionStatus status);
}
