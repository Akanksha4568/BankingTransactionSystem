
package com.banking.service;

import com.banking.model.TransactionAudit;
import com.banking.repository.TransactionAuditRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionAuditService {

    private final TransactionAuditRepository auditRepository;

    public TransactionAuditService(TransactionAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public List<TransactionAudit> getAuditHistory(Long transactionId) {
        return auditRepository.findByTransactionId(transactionId);
    }
}
