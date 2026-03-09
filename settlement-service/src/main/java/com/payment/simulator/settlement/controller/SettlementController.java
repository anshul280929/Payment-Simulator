package com.payment.simulator.settlement.controller;

import com.payment.simulator.settlement.entity.SettlementBatch;
import com.payment.simulator.settlement.entity.SettlementEntry;
import com.payment.simulator.settlement.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping("/process")
    public ResponseEntity<SettlementBatch> triggerSettlement() {
        SettlementBatch batch = settlementService.processSettlementBatch();
        if (batch == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(batch);
    }

    @GetMapping("/batches")
    public ResponseEntity<List<SettlementBatch>> getAllBatches() {
        return ResponseEntity.ok(settlementService.getAllBatches());
    }

    @GetMapping("/batches/{batchId}")
    public ResponseEntity<SettlementBatch> getBatch(@PathVariable String batchId) {
        return ResponseEntity.ok(settlementService.getBatch(batchId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<SettlementEntry>> getPendingEntries() {
        return ResponseEntity.ok(settlementService.getPendingEntries());
    }
}
