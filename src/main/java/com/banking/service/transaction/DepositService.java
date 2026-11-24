
package com.banking.service.transaction;

import com.banking.model.Account;
import com.banking.model.BankTransaction;
import com.banking.model.TransactionAudit;
import com.banking.repository.AccountRepository;
import com.banking.repository.BankTransactionRepository;
import com.banking.repository.TransactionAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class DepositService {

    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final TransactionAuditRepository auditRepository;
    private final TransactionValidator transactionValidator;

    public DepositService(
            AccountRepository accountRepository,
            BankTransactionRepository transactionRepository,
            TransactionAuditRepository auditRepository,
            TransactionValidator transactionValidator) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.auditRepository = auditRepository;
        this.transactionValidator = transactionValidator;
    }

    @Transactional
    public BankTransaction deposit(
            Long accountId,
            BigDecimal amount,
            String idempotencyKey,
            Long actorUserId) {

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Idempotency key is required."
            );
        }

        transactionValidator.validateAmount(amount);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account does not exist.")
                );

        transactionValidator.validateAccount(account);

        // Check whether this request was already processed.
        Optional<BankTransaction> existingTransaction =
                transactionRepository.findByIdempotencyKey(idempotencyKey);

        if (existingTransaction.isPresent()) {

            BankTransaction existing = existingTransaction.get();

            boolean sameRequest =
                    "DEPOSIT".equals(existing.getTransactionType())
                            && accountId.equals(existing.getToAccountId())
                            && amount.compareTo(existing.getAmount()) == 0;

            if (!sameRequest) {
                throw new IllegalArgumentException(
                        "Idempotency key has already been used for another transaction."
                );
            }

            return existing;
        }

        // Create a pending transaction record.
        BankTransaction transaction = new BankTransaction();

        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setFromAccountId(null);
        transaction.setToAccountId(accountId);
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setStatus("PENDING");
        transaction.setDescription("Account deposit");

        Long transactionId = transactionRepository.save(transaction);

        // Increase the account balance.
        int updatedRows =
                accountRepository.creditAccountBalance(accountId, amount);

        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "Deposit failed. Account balance was not updated."
            );
        }

        // Mark the transaction as successful.
        transactionRepository.updateStatus(transactionId, "SUCCESS");

        // Record the audit entry.
        TransactionAudit audit = new TransactionAudit();

        audit.setTransactionId(transactionId);
        audit.setActorUserId(actorUserId);
        audit.setAction("DEPOSIT_SUCCESS");
        audit.setDetails("Deposit of " + amount + " completed successfully.");

        auditRepository.save(audit);

        return transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new IllegalStateException("Transaction record not found.")
                );
    }
}
