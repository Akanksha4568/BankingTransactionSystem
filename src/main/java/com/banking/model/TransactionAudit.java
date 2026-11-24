// purpose is to maintain a record of important
// activities performed in the banking system,
// such as transaction processing, approvals and failures.

package com.banking.model;

import java.time.LocalDateTime;

public class TransactionAudit {

    private Long auditId;  //Unique audit record ID
    private Long transactionId;
    private Long actorUserId;  //User who performed the action
    private String action;
    private String details;  //Additional information
    private LocalDateTime createdAt;

    public TransactionAudit() {
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(Long actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}