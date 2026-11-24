
package com.banking.service;

import com.banking.model.FailedTransaction;
import com.banking.repository.FailedTransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FailedTransactionService {

    private final FailedTransactionRepository failedTransactionRepository;

    public FailedTransactionService(FailedTransactionRepository failedTransactionRepository) {
        this.failedTransactionRepository = failedTransactionRepository;
    }

    public List<FailedTransaction> getAllFailedTransactions() {
        return failedTransactionRepository.findAll();
    }

    public List<FailedTransaction> getFailedTransactionsByTransactionId(Long transactionId) {
        return failedTransactionRepository.findByTransactionId(transactionId);
    }
}
