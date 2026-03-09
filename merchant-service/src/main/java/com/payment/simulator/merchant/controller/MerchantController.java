package com.payment.simulator.merchant.controller;

import com.payment.simulator.common.exception.ErrorResponse;
import com.payment.simulator.merchant.entity.Merchant;
import com.payment.simulator.merchant.service.MerchantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping
    public ResponseEntity<Merchant> createMerchant(@RequestBody Merchant merchant) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantService.createMerchant(merchant));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Merchant> getMerchant(@PathVariable Long id) {
        return ResponseEntity.ok(merchantService.getMerchant(id));
    }

    @GetMapping
    public ResponseEntity<List<Merchant>> getAllMerchants() {
        return ResponseEntity.ok(merchantService.getAllMerchants());
    }

    @PostMapping("/{id}/api-key")
    public ResponseEntity<Merchant> regenerateApiKey(@PathVariable Long id) {
        return ResponseEntity.ok(merchantService.regenerateApiKey(id));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Bad Request", e.getMessage()));
    }
}
