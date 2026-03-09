package com.payment.simulator.fraud.entity;

import com.payment.simulator.common.enums.FraudCheckResult;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_check_history")
public class FraudCheckHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardToken;
    private Long merchantId;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudCheckResult result;

    private int riskScore;
    private String triggeredRules;

    @Column(nullable = false, updatable = false)
    private LocalDateTime checkedAt = LocalDateTime.now();

    public FraudCheckHistory() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCardToken() { return cardToken; }
    public void setCardToken(String cardToken) { this.cardToken = cardToken; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public FraudCheckResult getResult() { return result; }
    public void setResult(FraudCheckResult result) { this.result = result; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getTriggeredRules() { return triggeredRules; }
    public void setTriggeredRules(String triggeredRules) { this.triggeredRules = triggeredRules; }

    public LocalDateTime getCheckedAt() { return checkedAt; }
}
