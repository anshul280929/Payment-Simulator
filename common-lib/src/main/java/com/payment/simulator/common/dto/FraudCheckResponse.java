package com.payment.simulator.common.dto;

import com.payment.simulator.common.enums.FraudCheckResult;

import java.time.LocalDateTime;
import java.util.List;

public class FraudCheckResponse {

    private FraudCheckResult result;
    private int riskScore;
    private List<String> triggeredRules;
    private String message;
    private LocalDateTime checkedAt;

    public FraudCheckResponse() {
        this.checkedAt = LocalDateTime.now();
    }

    public static FraudCheckResponse pass() {
        FraudCheckResponse resp = new FraudCheckResponse();
        resp.result = FraudCheckResult.PASS;
        resp.riskScore = 0;
        resp.message = "No fraud indicators detected";
        resp.triggeredRules = List.of();
        return resp;
    }

    public static FraudCheckResponse block(int riskScore, List<String> rules) {
        FraudCheckResponse resp = new FraudCheckResponse();
        resp.result = FraudCheckResult.BLOCK;
        resp.riskScore = riskScore;
        resp.triggeredRules = rules;
        resp.message = "Transaction blocked by fraud detection";
        return resp;
    }

    public static FraudCheckResponse review(int riskScore, List<String> rules) {
        FraudCheckResponse resp = new FraudCheckResponse();
        resp.result = FraudCheckResult.REVIEW;
        resp.riskScore = riskScore;
        resp.triggeredRules = rules;
        resp.message = "Transaction flagged for review";
        return resp;
    }

    public FraudCheckResult getResult() { return result; }
    public void setResult(FraudCheckResult result) { this.result = result; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public List<String> getTriggeredRules() { return triggeredRules; }
    public void setTriggeredRules(List<String> triggeredRules) { this.triggeredRules = triggeredRules; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }
}
