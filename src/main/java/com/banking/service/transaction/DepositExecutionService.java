
package com.banking.service.transaction;

import com.banking.model.BankTransaction;
import com.banking.model.Account;
import com.banking.repository.AccountRepository;
import com.banking.repository.BankTransactionRepository;
import com.banking.service.failure.TransactionFailureProcessor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DepositExecutionService {

    private final DepositService depositService;
    private final TransactionFailureProcessor failureProcessor;
    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;

    public DepositExecutionService(
            DepositService depositService,
            TransactionFailureProcessor failureProcessor,
            AccountRepository accountRepository,
            BankTransactionRepository transactionRepository) {

        this.depositService = depositService;
        this.failureProcessor = failureProcessor;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public BankTransaction executeDeposit(
            Long accountId,
            BigDecimal amount,
            String idempotencyKey,
            Long actorUserId) {

        try {
            return depositService.deposit(
                    accountId, amount, idempotencyKey, actorUserId
            );

        } catch (RuntimeException exception) {

            boolean validRequest =
                    accountId != null
                            && amount != null
                            && amount.compareTo(BigDecimal.ZERO) > 0
                            && idempotencyKey != null
                            && !idempotencyKey.trim().isEmpty();

            if (validRequest) {
                try {
                    Account account = accountRepository.findById(accountId)
                            .orElse(null);

                    boolean keyAlreadyUsed =
                            transactionRepository
                                    .findByIdempotencyKey(idempotencyKey)
                                    .isPresent();

                    if (account != null && !keyAlreadyUsed) {

                        BankTransaction failedTransaction =
                                new BankTransaction();

                        failedTransaction.setIdempotencyKey(idempotencyKey);
                        failedTransaction.setToAccountId(accountId);
                        failedTransaction.setTransactionType("DEPOSIT");
                        failedTransaction.setAmount(amount);
                        failedTransaction.setDescription("Account deposit");

                        failureProcessor.recordFailure(
                                failedTransaction, actorUserId, exception
                        );
                    }

                } catch (RuntimeException recordingException) {
                    exception.addSuppressed(recordingException);
                }
            }

            throw exception;
        }
    }
}
