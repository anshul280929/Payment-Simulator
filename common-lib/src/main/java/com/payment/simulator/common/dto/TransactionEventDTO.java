package com.payment.simulator.common.dto;

import com.payment.simulator.common.enums.TransactionStatus;

import java.time.LocalDateTime;

public class TransactionEventDTO {

    private Long id;
    private TransactionStatus fromStatus;
    private TransactionStatus toStatus;
    private String description;
    private String performedBy;
    private LocalDateTime timestamp;

    public TransactionEventDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TransactionStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(TransactionStatus fromStatus) { this.fromStatus = fromStatus; }

    public TransactionStatus getToStatus() { return toStatus; }
    public void setToStatus(TransactionStatus toStatus) { this.toStatus = toStatus; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
