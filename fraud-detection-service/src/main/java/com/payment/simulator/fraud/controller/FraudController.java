package com.payment.simulator.fraud.controller;

import com.payment.simulator.common.dto.FraudCheckRequest;
import com.payment.simulator.common.dto.FraudCheckResponse;
import com.payment.simulator.fraud.service.FraudDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fraud")
public class FraudController {

    private final FraudDetectionService fraudDetectionService;

    public FraudController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @PostMapping("/check")
    public ResponseEntity<FraudCheckResponse> checkFraud(@RequestBody FraudCheckRequest request) {
        return ResponseEntity.ok(fraudDetectionService.check(request));
    }
}
