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
public class TransferService {

    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final TransactionAuditRepository auditRepository;
    private final TransactionValidator transactionValidator;

    public TransferService(AccountRepository accountRepository,
                           BankTransactionRepository transactionRepository,
                           TransactionAuditRepository auditRepository,
                           TransactionValidator transactionValidator) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.auditRepository = auditRepository;
        this.transactionValidator = transactionValidator;
    }

    @Transactional
    public BankTransaction transfer(Long fromAccountId, Long toAccountId,
                                    BigDecimal amount, String idempotencyKey,
                                    Long actorUserId) {

        // Step 1: Validate account IDs
        if (fromAccountId == null || toAccountId == null) {
            throw new IllegalArgumentException("Account IDs are required.");
        }

        // Step 2: Validate the idempotency key
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Idempotency key is required.");
        }

        // Step 3: Validate the amount
        transactionValidator.validateAmount(amount);

        // Step 4: Prevent transfers to the same account
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException(
                    "Cannot transfer money to the same account.");
        }

        // Step 5: Check whether this request was already processed
        Optional<BankTransaction> existingTransaction =
                transactionRepository.findByIdempotencyKey(idempotencyKey);

        if (existingTransaction.isPresent()) {
            BankTransaction existing = existingTransaction.get();

            boolean sameRequest =
                    "TRANSFER".equals(existing.getTransactionType())
                            && fromAccountId.equals(existing.getFromAccountId())
                            && toAccountId.equals(existing.getToAccountId())
                            && amount.compareTo(existing.getAmount()) == 0;

            if (!sameRequest) {
                throw new IllegalArgumentException(
                        "Idempotency key has already been used for another transaction.");
            }

            return existing;
        }

        // Step 6: Lock accounts in ascending ID order to avoid deadlocks
        Account firstLocked;
        Account secondLocked;

        if (fromAccountId.compareTo(toAccountId) < 0) {
            firstLocked = accountRepository.findByIdForUpdate(fromAccountId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Sender account does not exist."));

            secondLocked = accountRepository.findByIdForUpdate(toAccountId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Receiver account does not exist."));
        } else {
            firstLocked = accountRepository.findByIdForUpdate(toAccountId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Receiver account does not exist."));

            secondLocked = accountRepository.findByIdForUpdate(fromAccountId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Sender account does not exist."));
        }

        // Step 7: Identify sender and receiver
        Account fromAccount = fromAccountId.equals(firstLocked.getAccountId())
                ? firstLocked : secondLocked;

        Account toAccount = toAccountId.equals(firstLocked.getAccountId())
                ? firstLocked : secondLocked;

        // Step 8: Validate both accounts and sender's balance
        transactionValidator.validateTransferAccounts(fromAccount, toAccount);
        if (!fromAccount.getCurrency().equalsIgnoreCase(toAccount.getCurrency())) {
            throw new IllegalArgumentException(
                    "Transfer between different currencies is not supported.");
        }
        transactionValidator.validateSufficientBalance(fromAccount, amount);

        // Step 9: Create a pending transaction record
        BankTransaction transaction = new BankTransaction();
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setFromAccountId(fromAccountId);
        transaction.setToAccountId(toAccountId);
        transaction.setTransactionType("TRANSFER");
        transaction.setAmount(amount);
        transaction.setStatus("PENDING");
        transaction.setDescription("Account-to-account transfer");

        Long transactionId = transactionRepository.save(transaction);

        // Step 10: Debit the sender's account
        int debitRows =
                accountRepository.debitAccountBalance(fromAccountId, amount);

        if (debitRows != 1) {
            throw new IllegalStateException("Transfer failed while debiting sender.");
        }

        // Step 11: Credit the receiver's account
        int creditRows =
                accountRepository.creditAccountBalance(toAccountId, amount);

        if (creditRows != 1) {
            throw new IllegalStateException("Transfer failed while crediting receiver.");
        }

        // Step 12: Mark the transaction as successful
        int statusRows =
                transactionRepository.updateStatus(transactionId, "SUCCESS");

        if (statusRows != 1) {
            throw new IllegalStateException("Could not update transfer status.");
        }

        // Step 13: Save the audit record
        TransactionAudit audit = new TransactionAudit();
        audit.setTransactionId(transactionId);
        audit.setActorUserId(actorUserId);
        audit.setAction("TRANSFER_SUCCESS");
        audit.setDetails("Transfer of " + amount + " completed successfully.");

        auditRepository.save(audit);

        // Step 14: Return the completed transaction
        return transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new IllegalStateException("Transaction record not found."));
    }
}
