package com.banking.model;

import java.time.LocalDateTime;

public class FailedTransaction {

    private Long failureId;
    private Long transactionId;
    private String failureReason;
    private LocalDateTime failedAt;

    public FailedTransaction() {
    }

    public Long getFailureId() {
        return failureId;
    }

    public void setFailureId(Long failureId) {
        this.failureId = failureId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }
}