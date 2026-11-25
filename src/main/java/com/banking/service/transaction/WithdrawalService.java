
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
public class WithdrawalService {

    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final TransactionAuditRepository auditRepository;
    private final TransactionValidator transactionValidator;

    public WithdrawalService(AccountRepository accountRepository,
                             BankTransactionRepository transactionRepository,
                             TransactionAuditRepository auditRepository,
                             TransactionValidator transactionValidator) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.auditRepository = auditRepository;
        this.transactionValidator = transactionValidator;
    }

    @Transactional
    public BankTransaction withdraw(Long accountId, BigDecimal amount,
                                    String idempotencyKey, Long actorUserId) {

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Idempotency key is required.");
        }

        transactionValidator.validateAmount(amount);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account does not exist."));

        transactionValidator.validateAccount(account);

        Optional<BankTransaction> existingTransaction =
                transactionRepository.findByIdempotencyKey(idempotencyKey);

        if (existingTransaction.isPresent()) {
            BankTransaction existing = existingTransaction.get();

            boolean sameRequest =
                    "WITHDRAWAL".equals(existing.getTransactionType())
                            && accountId.equals(existing.getFromAccountId())
                            && amount.compareTo(existing.getAmount()) == 0;

            if (!sameRequest) {
                throw new IllegalArgumentException(
                        "Idempotency key has already been used for another transaction.");
            }

            return existing;
        }

        transactionValidator.validateSufficientBalance(account, amount);

        BankTransaction transaction = new BankTransaction();
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setFromAccountId(accountId);
        transaction.setToAccountId(null);
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setAmount(amount);
        transaction.setStatus("PENDING");
        transaction.setDescription("Account withdrawal");

        Long transactionId = transactionRepository.save(transaction);

        int updatedRows =
                accountRepository.debitAccountBalance(accountId, amount);

        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "Withdrawal failed. Insufficient balance or inactive account.");
        }

        int statusUpdated = transactionRepository.updateStatus(transactionId, "SUCCESS");

        if (statusUpdated != 1) {
            throw new IllegalStateException(
                    "Withdrawal failed. Transaction status could not be updated.");
        }

        TransactionAudit audit = new TransactionAudit();
        audit.setTransactionId(transactionId);
        audit.setActorUserId(actorUserId);
        audit.setAction("WITHDRAWAL_SUCCESS");
        audit.setDetails("Withdrawal of " + amount + " completed successfully.");

        auditRepository.save(audit);

        return transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new IllegalStateException("Transaction record not found."));
    }
}
