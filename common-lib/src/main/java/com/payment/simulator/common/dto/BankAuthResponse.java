package com.payment.simulator.common.dto;

import java.time.LocalDateTime;

public class BankAuthResponse {

    private boolean approved;
    private String authorizationCode;
    private String declineReason;
    private String responseCode;
    private LocalDateTime processedAt;

    public BankAuthResponse() {
        this.processedAt = LocalDateTime.now();
    }

    public static BankAuthResponse approved(String authCode) {
        BankAuthResponse resp = new BankAuthResponse();
        resp.approved = true;
        resp.authorizationCode = authCode;
        resp.responseCode = "00";
        return resp;
    }

    public static BankAuthResponse declined(String reason, String responseCode) {
        BankAuthResponse resp = new BankAuthResponse();
        resp.approved = false;
        resp.declineReason = reason;
        resp.responseCode = responseCode;
        return resp;
    }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public String getAuthorizationCode() { return authorizationCode; }
    public void setAuthorizationCode(String authorizationCode) { this.authorizationCode = authorizationCode; }

    public String getDeclineReason() { return declineReason; }
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
