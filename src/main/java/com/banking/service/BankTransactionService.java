
package com.banking.service;

import com.banking.model.BankTransaction;
import com.banking.repository.BankTransactionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BankTransactionService {

    private final BankTransactionRepository transactionRepository;

    public BankTransactionService(BankTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Optional<BankTransaction> getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId);
    }

    //Suppose a user clicks the transfer button twice because the page is slow.
    //Without idempotency, the same transfer might happen twice.
    //Our system will use a unique idempotency key to identify repeated requests and prevent duplicate transactions.

    public Optional<BankTransaction> getTransactionByIdempotencyKey(String idempotencyKey) {
        return transactionRepository.findByIdempotencyKey(idempotencyKey);
    }
}
