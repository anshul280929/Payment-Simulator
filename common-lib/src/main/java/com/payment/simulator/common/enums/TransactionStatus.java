package com.payment.simulator.common.enums;

public enum TransactionStatus {
    PENDING,
    AUTHORIZED,
    CAPTURED,
    SETTLED,
    REFUNDED,
    PARTIALLY_REFUNDED,
    DECLINED,
    FAILED,
    CHARGEBACK_INITIATED,
    CHARGEBACK_RESOLVED,
    VOIDED,
    EXPIRED
}
