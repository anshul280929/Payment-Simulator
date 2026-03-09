package com.payment.simulator.settlement.service;

import com.payment.simulator.common.event.PaymentEvent;
import com.payment.simulator.settlement.entity.SettlementBatch;
import com.payment.simulator.settlement.entity.SettlementEntry;
import com.payment.simulator.settlement.repository.SettlementBatchRepository;
import com.payment.simulator.settlement.repository.SettlementEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);

    private final SettlementBatchRepository batchRepository;
    private final SettlementEntryRepository entryRepository;

    public SettlementService(SettlementBatchRepository batchRepository,
                             SettlementEntryRepository entryRepository) {
        this.batchRepository = batchRepository;
        this.entryRepository = entryRepository;
    }

    @RabbitListener(queues = "settlement.captured.queue")
    public void handleCapturedPayment(PaymentEvent event) {
        log.info("Received captured payment for settlement: txn={}", event.getTransactionId());

        SettlementEntry entry = new SettlementEntry();
        entry.setTransactionId(event.getTransactionId());
        entry.setMerchantId(event.getMerchantId());
        entry.setAmount(event.getAmount());
        entry.setCurrency(event.getCurrency());
        entry.setStatus("PENDING");
        entryRepository.save(entry);
    }

    @Scheduled(cron = "${settlement.cron}")
    @Transactional
    public void runDailySettlement() {
        log.info("Starting daily settlement batch...");
        processSettlementBatch();
    }

    @Transactional
    public SettlementBatch processSettlementBatch() {
        List<SettlementEntry> pendingEntries = entryRepository.findByStatus("PENDING");

        if (pendingEntries.isEmpty()) {
            log.info("No pending entries to settle");
            return null;
        }

        SettlementBatch batch = new SettlementBatch();
        batch.setBatchId("BATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        batch.setStatus("PROCESSING");

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SettlementEntry entry : pendingEntries) {
            entry.setStatus("SETTLED");
            batch.addEntry(entry);
            totalAmount = totalAmount.add(entry.getAmount());
        }

        batch.setTotalTransactions(pendingEntries.size());
        batch.setTotalAmount(totalAmount);
        batch.setStatus("COMPLETED");
        batch.setCompletedAt(LocalDateTime.now());

        batchRepository.save(batch);

        log.info("Settlement batch {} completed: {} transactions, total amount {}",
                batch.getBatchId(), batch.getTotalTransactions(), batch.getTotalAmount());

        return batch;
    }

    public List<SettlementBatch> getAllBatches() {
        return batchRepository.findAll();
    }

    public SettlementBatch getBatch(String batchId) {
        return batchRepository.findByBatchId(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found: " + batchId));
    }

    public List<SettlementEntry> getPendingEntries() {
        return entryRepository.findByStatus("PENDING");
    }
}
