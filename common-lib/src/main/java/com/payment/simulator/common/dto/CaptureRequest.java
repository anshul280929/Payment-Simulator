package com.payment.simulator.common.dto;

import java.math.BigDecimal;

public class CaptureRequest {
    private BigDecimal amount;

    public CaptureRequest() {}

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
