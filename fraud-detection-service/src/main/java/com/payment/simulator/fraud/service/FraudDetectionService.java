package com.payment.simulator.fraud.service;

import com.payment.simulator.common.dto.FraudCheckRequest;
import com.payment.simulator.common.dto.FraudCheckResponse;
import com.payment.simulator.common.enums.FraudCheckResult;
import com.payment.simulator.fraud.entity.FraudCheckHistory;
import com.payment.simulator.fraud.repository.FraudCheckHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FraudDetectionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);

    private final FraudCheckHistoryRepository historyRepository;

    @Value("${fraud.rules.block-amount-threshold:10000}")
    private BigDecimal blockAmountThreshold;

    @Value("${fraud.rules.review-amount-threshold:5000}")
    private BigDecimal reviewAmountThreshold;

    @Value("${fraud.rules.velocity-window-seconds:60}")
    private int velocityWindowSeconds;

    @Value("${fraud.rules.velocity-max-transactions:3}")
    private int velocityMaxTransactions;

    public FraudDetectionService(FraudCheckHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public FraudCheckResponse check(FraudCheckRequest request) {
        List<String> triggeredRules = new ArrayList<>();
        int riskScore = 0;
        FraudCheckResult result = FraudCheckResult.PASS;

        // Rule 1: Amount > $10,000 → BLOCK
        if (request.getAmount().compareTo(blockAmountThreshold) > 0) {
            triggeredRules.add("AMOUNT_EXCEEDS_BLOCK_THRESHOLD");
            riskScore += 100;
            result = FraudCheckResult.BLOCK;
        }

        // Rule 2: Velocity check — too many transactions in short window
        if (request.getCardToken() != null) {
            LocalDateTime windowStart = LocalDateTime.now().minusSeconds(velocityWindowSeconds);
            long recentCount = historyRepository.countByCardTokenAndCheckedAtAfter(
                    request.getCardToken(), windowStart);
            if (recentCount >= velocityMaxTransactions) {
                triggeredRules.add("VELOCITY_CHECK_FAILED");
                riskScore += 80;
                result = FraudCheckResult.BLOCK;
            }
        }

        // Rule 3: Amount > $5,000 → REVIEW (but don't downgrade a BLOCK)
        if (result != FraudCheckResult.BLOCK &&
            request.getAmount().compareTo(reviewAmountThreshold) > 0) {
            triggeredRules.add("AMOUNT_EXCEEDS_REVIEW_THRESHOLD");
            riskScore += 50;
            result = FraudCheckResult.REVIEW;
        }

        // Rule 4: Mismatched billing/shipping address → REVIEW
        if (request.getBillingAddress() != null && request.getShippingAddress() != null
                && !request.getBillingAddress().equalsIgnoreCase(request.getShippingAddress())) {
            triggeredRules.add("ADDRESS_MISMATCH");
            riskScore += 30;
            if (result == FraudCheckResult.PASS) {
                result = FraudCheckResult.REVIEW;
            }
        }

        // Save check history
        FraudCheckHistory history = new FraudCheckHistory();
        history.setCardToken(request.getCardToken());
        history.setMerchantId(request.getMerchantId());
        history.setAmount(request.getAmount());
        history.setCurrency(request.getCurrency());
        history.setResult(result);
        history.setRiskScore(riskScore);
        history.setTriggeredRules(String.join(",", triggeredRules));
        historyRepository.save(history);

        log.info("Fraud check result={}, score={}, rules={}", result, riskScore, triggeredRules);

        if (result == FraudCheckResult.BLOCK) {
            return FraudCheckResponse.block(riskScore, triggeredRules);
        } else if (result == FraudCheckResult.REVIEW) {
            return FraudCheckResponse.review(riskScore, triggeredRules);
        }
        return FraudCheckResponse.pass();
    }
}
