package com.payment.simulator.transaction.controller;

import com.payment.simulator.common.dto.TransactionDTO;
import com.payment.simulator.common.enums.TransactionStatus;
import com.payment.simulator.common.exception.ErrorResponse;
import com.payment.simulator.transaction.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> searchTransactions(
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) TransactionStatus status) {
        return ResponseEntity.ok(transactionService.searchTransactions(merchantId, status));
    }

    @PostMapping("/{transactionId}/chargeback")
    public ResponseEntity<TransactionDTO> initiateChargeback(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.initiateChargeback(transactionId));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Bad Request", e.getMessage()));
    }
}
