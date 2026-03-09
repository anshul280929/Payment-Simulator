package com.payment.simulator.settlement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_entries")
public class SettlementEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private SettlementBatch batch;

    @Column(nullable = false)
    private String transactionId;

    private Long merchantId;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    private String currency;

    @Column(nullable = false)
    private String status; // PENDING, SETTLED, FAILED

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SettlementEntry() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SettlementBatch getBatch() { return batch; }
    public void setBatch(SettlementBatch batch) { this.batch = batch; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
