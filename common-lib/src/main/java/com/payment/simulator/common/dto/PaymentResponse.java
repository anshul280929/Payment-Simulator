package com.payment.simulator.common.dto;

import com.payment.simulator.common.enums.Currency;
import com.payment.simulator.common.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private String transactionId;
    private TransactionStatus status;
    private BigDecimal amount;
    private Currency currency;
    private String maskedCardNumber;
    private String cardToken;
    private String authorizationCode;
    private String message;
    private LocalDateTime timestamp;

    public PaymentResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public static PaymentResponse success(String transactionId, TransactionStatus status,
                                          BigDecimal amount, Currency currency,
                                          String maskedCard, String cardToken, String authCode) {
        PaymentResponse response = new PaymentResponse();
        response.transactionId = transactionId;
        response.status = status;
        response.amount = amount;
        response.currency = currency;
        response.maskedCardNumber = maskedCard;
        response.cardToken = cardToken;
        response.authorizationCode = authCode;
        response.message = "Transaction processed successfully";
        return response;
    }

    public static PaymentResponse declined(String transactionId, String reason) {
        PaymentResponse response = new PaymentResponse();
        response.transactionId = transactionId;
        response.status = TransactionStatus.DECLINED;
        response.message = reason;
        return response;
    }

    public static PaymentResponse error(String message) {
        PaymentResponse response = new PaymentResponse();
        response.status = TransactionStatus.FAILED;
        response.message = message;
        return response;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }

    public String getMaskedCardNumber() { return maskedCardNumber; }
    public void setMaskedCardNumber(String maskedCardNumber) { this.maskedCardNumber = maskedCardNumber; }

    public String getCardToken() { return cardToken; }
    public void setCardToken(String cardToken) { this.cardToken = cardToken; }

    public String getAuthorizationCode() { return authorizationCode; }
    public void setAuthorizationCode(String authorizationCode) { this.authorizationCode = authorizationCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
