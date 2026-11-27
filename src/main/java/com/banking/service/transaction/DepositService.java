
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

        // Step 1: Validate idempotency key
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Idempotency key is required."
            );
        }

        // Step 2: Validate amount
        transactionValidator.validateAmount(amount);

        // Step 3: Lock the account row
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account does not exist."
                        )
                );

        // Step 4: Validate account status
        transactionValidator.validateAccount(account);

        // Step 5: Check idempotency key
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

        // Step 6: Create pending transaction
        BankTransaction transaction = new BankTransaction();

        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setFromAccountId(null);
        transaction.setToAccountId(accountId);
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setStatus("PENDING");
        transaction.setDescription("Account deposit");

        // Step 7: Save transaction if key is unused
        Optional<Long> savedTransactionId =
                transactionRepository.saveIfAbsent(transaction);

        if (!savedTransactionId.isPresent()) {

            BankTransaction existing =
                    transactionRepository.findByIdempotencyKey(idempotencyKey)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Transaction already exists but could not be retrieved."
                                    )
                            );

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

        Long transactionId = savedTransactionId.get();

        // Step 8: Credit account balance
        int updatedRows =
                accountRepository.creditAccountBalance(accountId, amount);

        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "Deposit failed. Account balance was not updated."
            );
        }

        // Step 9: Update transaction status
        int statusUpdated =
                transactionRepository.updateStatus(
                        transactionId, "SUCCESS"
                );

        if (statusUpdated != 1) {
            throw new IllegalStateException(
                    "Deposit failed. Transaction status could not be updated."
            );
        }

        // Step 10: Save audit record
        TransactionAudit audit = new TransactionAudit();

        audit.setTransactionId(transactionId);
        audit.setActorUserId(actorUserId);
        audit.setAction("DEPOSIT_SUCCESS");
        audit.setDetails(
                "Deposit of " + amount + " completed successfully."
        );

        auditRepository.save(audit);

        // Step 11: Return completed transaction
        return transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Transaction record not found."
                        )
                );
    }
}
