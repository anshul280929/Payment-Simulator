package com.payment.simulator.gateway.controller;

import com.payment.simulator.common.dto.*;
import com.payment.simulator.common.exception.ErrorResponse;
import com.payment.simulator.common.exception.FraudDetectedException;
import com.payment.simulator.common.exception.PaymentException;
import com.payment.simulator.gateway.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/authorize")
    public ResponseEntity<PaymentResponse> authorize(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.authorize(request));
    }

    @PostMapping("/{transactionId}/capture")
    public ResponseEntity<PaymentResponse> capture(@PathVariable String transactionId,
                                                   @RequestBody CaptureRequest request) {
        return ResponseEntity.ok(paymentService.capture(transactionId, request));
    }

    @PostMapping("/{transactionId}/refund")
    public ResponseEntity<PaymentResponse> refund(@PathVariable String transactionId,
                                                  @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.refund(transactionId, request));
    }

    @GetMapping("/{transactionId}/status")
    public ResponseEntity<PaymentResponse> getStatus(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getStatus(transactionId));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Payment Error", e.getMessage(), e.getErrorCode()));
    }

    @ExceptionHandler(FraudDetectedException.class)
    public ResponseEntity<ErrorResponse> handleFraudException(FraudDetectedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(403, "Fraud Detected", e.getMessage(), "FRAUD_BLOCK"));
    }
}
