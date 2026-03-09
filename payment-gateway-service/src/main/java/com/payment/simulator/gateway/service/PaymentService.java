package com.payment.simulator.gateway.service;

import com.payment.simulator.common.audit.AuditLog;
import com.payment.simulator.common.dto.*;
import com.payment.simulator.common.enums.FraudCheckResult;
import com.payment.simulator.common.enums.TransactionStatus;
import com.payment.simulator.common.event.PaymentEvent;
import com.payment.simulator.common.exception.FraudDetectedException;
import com.payment.simulator.common.exception.PaymentException;
import com.payment.simulator.common.util.CardMasker;
import com.payment.simulator.gateway.entity.CardToken;
import com.payment.simulator.gateway.entity.PaymentRecord;
import com.payment.simulator.gateway.repository.PaymentRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRecordRepository paymentRecordRepository;
    private final TokenizationService tokenizationService;
    private final PaymentEventPublisher eventPublisher;
    private final RestTemplate restTemplate;

    @Value("${services.fraud-detection-url}")
    private String fraudDetectionUrl;

    @Value("${services.issuer-bank-url}")
    private String issuerBankUrl;

    public PaymentService(PaymentRecordRepository paymentRecordRepository,
                          TokenizationService tokenizationService,
                          PaymentEventPublisher eventPublisher,
                          RestTemplate restTemplate) {
        this.paymentRecordRepository = paymentRecordRepository;
        this.tokenizationService = tokenizationService;
        this.eventPublisher = eventPublisher;
        this.restTemplate = restTemplate;
    }

    @Transactional
    @AuditLog(action = "AUTHORIZE", entity = "Payment")
    public PaymentResponse authorize(PaymentRequest request) {
        String transactionId = "txn_" + UUID.randomUUID().toString().replace("-", "");
        log.info("Processing authorization: txn={}, amount={} {}, card=****{}",
                transactionId, request.getAmount(), request.getCurrency(),
                CardMasker.lastFour(request.getCardNumber()));

        // Step 1: Fraud check
        FraudCheckResponse fraudResult = performFraudCheck(request);
        if (fraudResult.getResult() == FraudCheckResult.BLOCK) {
            log.warn("Transaction blocked by fraud detection: txn={}, score={}",
                    transactionId, fraudResult.getRiskScore());
            PaymentRecord record = createPaymentRecord(transactionId, request, TransactionStatus.DECLINED);
            paymentRecordRepository.save(record);
            throw new FraudDetectedException("Transaction blocked: " + fraudResult.getMessage(),
                    fraudResult.getRiskScore());
        }

        // Step 2: Tokenize card data
        CardToken cardToken = tokenizationService.tokenize(
                request.getCardNumber(), request.getCardholderName(),
                request.getExpiryMonth(), request.getExpiryYear());

        // Step 3: Bank authorization
        BankAuthResponse bankResponse = performBankAuth(request);

        if (!bankResponse.isApproved()) {
            log.info("Bank declined transaction: txn={}, reason={}",
                    transactionId, bankResponse.getDeclineReason());
            PaymentRecord record = createPaymentRecord(transactionId, request, TransactionStatus.DECLINED);
            record.setCardToken(cardToken.getToken());
            record.setMaskedCardNumber(CardMasker.mask(request.getCardNumber()));
            paymentRecordRepository.save(record);
            return PaymentResponse.declined(transactionId, bankResponse.getDeclineReason());
        }

        // Step 4: Save authorized payment
        PaymentRecord record = createPaymentRecord(transactionId, request, TransactionStatus.AUTHORIZED);
        record.setCardToken(cardToken.getToken());
        record.setMaskedCardNumber(CardMasker.mask(request.getCardNumber()));
        record.setAuthorizationCode(bankResponse.getAuthorizationCode());
        paymentRecordRepository.save(record);

        // Step 5: Publish event
        eventPublisher.publishAuthorized(new PaymentEvent("AUTHORIZED", transactionId,
                request.getMerchantId(), request.getAmount(),
                request.getCurrency().name(), "AUTHORIZED"));

        log.info("Payment authorized: txn={}, authCode={}", transactionId, bankResponse.getAuthorizationCode());
        return PaymentResponse.success(transactionId, TransactionStatus.AUTHORIZED,
                request.getAmount(), request.getCurrency(),
                CardMasker.mask(request.getCardNumber()),
                cardToken.getToken(), bankResponse.getAuthorizationCode());
    }

    @Transactional
    @AuditLog(action = "CAPTURE", entity = "Payment")
    public PaymentResponse capture(String transactionId, CaptureRequest captureRequest) {
        PaymentRecord record = paymentRecordRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentException("Transaction not found: " + transactionId));

        if (record.getStatus() != TransactionStatus.AUTHORIZED) {
            throw new PaymentException("Cannot capture payment in status: " + record.getStatus());
        }

        BigDecimal captureAmount = captureRequest.getAmount() != null
                ? captureRequest.getAmount() : record.getAmount();

        if (captureAmount.compareTo(record.getAmount()) > 0) {
            throw new PaymentException("Capture amount exceeds authorized amount");
        }

        // Call bank to capture
        BankAuthRequest bankRequest = new BankAuthRequest();
        bankRequest.setAmount(captureAmount);
        bankRequest.setCardNumber(tokenizationService.decryptCardNumber(record.getCardToken()));
        try {
            restTemplate.postForObject(issuerBankUrl + "/api/bank/capture", bankRequest, BankAuthResponse.class);
        } catch (Exception e) {
            log.error("Bank capture failed for txn={}: {}", transactionId, e.getMessage());
            throw new PaymentException("Bank capture failed");
        }

        record.setCapturedAmount(captureAmount);
        record.setStatus(TransactionStatus.CAPTURED);
        paymentRecordRepository.save(record);

        eventPublisher.publishCaptured(new PaymentEvent("CAPTURED", transactionId,
                record.getMerchantId(), captureAmount,
                record.getCurrency().name(), "CAPTURED"));

        log.info("Payment captured: txn={}, amount={}", transactionId, captureAmount);
        return PaymentResponse.success(transactionId, TransactionStatus.CAPTURED,
                captureAmount, record.getCurrency(),
                record.getMaskedCardNumber(), record.getCardToken(), record.getAuthorizationCode());
    }

    @Transactional
    @AuditLog(action = "REFUND", entity = "Payment")
    public PaymentResponse refund(String transactionId, RefundRequest refundRequest) {
        PaymentRecord record = paymentRecordRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentException("Transaction not found: " + transactionId));

        if (record.getStatus() != TransactionStatus.CAPTURED &&
            record.getStatus() != TransactionStatus.PARTIALLY_REFUNDED) {
            throw new PaymentException("Cannot refund payment in status: " + record.getStatus());
        }

        BigDecimal refundAmount = refundRequest.getAmount() != null
                ? refundRequest.getAmount() : record.getCapturedAmount();

        BigDecimal totalRefunded = record.getRefundedAmount().add(refundAmount);
        if (totalRefunded.compareTo(record.getCapturedAmount()) > 0) {
            throw new PaymentException("Refund amount exceeds captured amount");
        }

        // Call bank to refund
        BankAuthRequest bankRequest = new BankAuthRequest();
        bankRequest.setAmount(refundAmount);
        bankRequest.setCardNumber(tokenizationService.decryptCardNumber(record.getCardToken()));
        try {
            restTemplate.postForObject(issuerBankUrl + "/api/bank/refund", bankRequest, BankAuthResponse.class);
        } catch (Exception e) {
            log.error("Bank refund failed for txn={}: {}", transactionId, e.getMessage());
            throw new PaymentException("Bank refund failed");
        }

        record.setRefundedAmount(totalRefunded);
        if (totalRefunded.compareTo(record.getCapturedAmount()) == 0) {
            record.setStatus(TransactionStatus.REFUNDED);
        } else {
            record.setStatus(TransactionStatus.PARTIALLY_REFUNDED);
        }
        paymentRecordRepository.save(record);

        eventPublisher.publishRefunded(new PaymentEvent("REFUNDED", transactionId,
                record.getMerchantId(), refundAmount,
                record.getCurrency().name(), record.getStatus().name()));

        log.info("Payment refunded: txn={}, amount={}, total_refunded={}",
                transactionId, refundAmount, totalRefunded);
        return PaymentResponse.success(transactionId, record.getStatus(),
                refundAmount, record.getCurrency(),
                record.getMaskedCardNumber(), record.getCardToken(), record.getAuthorizationCode());
    }

    public PaymentResponse getStatus(String transactionId) {
        PaymentRecord record = paymentRecordRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentException("Transaction not found: " + transactionId));

        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(record.getTransactionId());
        response.setStatus(record.getStatus());
        response.setAmount(record.getAmount());
        response.setCurrency(record.getCurrency());
        response.setMaskedCardNumber(record.getMaskedCardNumber());
        response.setCardToken(record.getCardToken());
        response.setAuthorizationCode(record.getAuthorizationCode());
        response.setMessage("Status: " + record.getStatus());
        return response;
    }

    private FraudCheckResponse performFraudCheck(PaymentRequest request) {
        try {
            FraudCheckRequest fraudRequest = new FraudCheckRequest();
            fraudRequest.setCardNumber(CardMasker.mask(request.getCardNumber()));
            fraudRequest.setAmount(request.getAmount());
            fraudRequest.setCurrency(request.getCurrency().name());
            fraudRequest.setMerchantId(request.getMerchantId());
            fraudRequest.setBillingAddress(request.getBillingAddress());
            fraudRequest.setShippingAddress(request.getShippingAddress());

            return restTemplate.postForObject(
                    fraudDetectionUrl + "/api/fraud/check", fraudRequest, FraudCheckResponse.class);
        } catch (FraudDetectedException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Fraud detection service unavailable, proceeding: {}", e.getMessage());
            return FraudCheckResponse.pass();
        }
    }

    private BankAuthResponse performBankAuth(PaymentRequest request) {
        try {
            BankAuthRequest bankRequest = new BankAuthRequest();
            bankRequest.setCardNumber(request.getCardNumber());
            bankRequest.setCardholderName(request.getCardholderName());
            bankRequest.setExpiryMonth(request.getExpiryMonth());
            bankRequest.setExpiryYear(request.getExpiryYear());
            bankRequest.setCvv(request.getCvv());
            bankRequest.setAmount(request.getAmount());
            bankRequest.setCurrency(request.getCurrency().name());

            return restTemplate.postForObject(
                    issuerBankUrl + "/api/bank/authorize", bankRequest, BankAuthResponse.class);
        } catch (Exception e) {
            log.error("Bank authorization failed: {}", e.getMessage());
            throw new PaymentException("Bank authorization failed: " + e.getMessage());
        }
    }

    private PaymentRecord createPaymentRecord(String transactionId, PaymentRequest request,
                                              TransactionStatus status) {
        PaymentRecord record = new PaymentRecord();
        record.setTransactionId(transactionId);
        record.setMerchantId(request.getMerchantId());
        record.setAmount(request.getAmount());
        record.setCurrency(request.getCurrency());
        record.setPaymentMethod(request.getPaymentMethod());
        record.setStatus(status);
        record.setDescription(request.getDescription());
        return record;
    }
}
