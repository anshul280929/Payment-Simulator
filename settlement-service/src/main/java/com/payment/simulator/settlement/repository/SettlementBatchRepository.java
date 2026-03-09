package com.payment.simulator.settlement.repository;

import com.payment.simulator.settlement.entity.SettlementBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, Long> {
    Optional<SettlementBatch> findByBatchId(String batchId);
    List<SettlementBatch> findByStatus(String status);
}
