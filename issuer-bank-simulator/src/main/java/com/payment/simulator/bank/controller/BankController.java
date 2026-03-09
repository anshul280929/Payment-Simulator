package com.payment.simulator.bank.controller;

import com.payment.simulator.bank.service.BankService;
import com.payment.simulator.common.dto.BankAuthRequest;
import com.payment.simulator.common.dto.BankAuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bank")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping("/authorize")
    public ResponseEntity<BankAuthResponse> authorize(@RequestBody BankAuthRequest request) {
        return ResponseEntity.ok(bankService.authorize(request));
    }

    @PostMapping("/capture")
    public ResponseEntity<BankAuthResponse> capture(@RequestBody BankAuthRequest request) {
        return ResponseEntity.ok(bankService.capture(request));
    }

    @PostMapping("/refund")
    public ResponseEntity<BankAuthResponse> refund(@RequestBody BankAuthRequest request) {
        return ResponseEntity.ok(bankService.refund(request));
    }
}
