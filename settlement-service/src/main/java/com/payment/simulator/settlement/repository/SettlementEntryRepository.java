package com.payment.simulator.settlement.repository;

import com.payment.simulator.settlement.entity.SettlementEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementEntryRepository extends JpaRepository<SettlementEntry, Long> {
    List<SettlementEntry> findByStatus(String status);
    List<SettlementEntry> findByMerchantId(Long merchantId);
}
