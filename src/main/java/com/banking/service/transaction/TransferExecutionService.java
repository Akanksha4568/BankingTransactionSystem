
package com.banking.service.transaction;

import com.banking.model.Account;
import com.banking.model.BankTransaction;
import com.banking.repository.AccountRepository;
import com.banking.repository.BankTransactionRepository;
import com.banking.service.failure.TransactionFailureProcessor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransferExecutionService {

    private final TransferService transferService;
    private final TransactionFailureProcessor failureProcessor;
    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;

    public TransferExecutionService(
            TransferService transferService,
            TransactionFailureProcessor failureProcessor,
            AccountRepository accountRepository,
            BankTransactionRepository transactionRepository) {

        this.transferService = transferService;
        this.failureProcessor = failureProcessor;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public BankTransaction executeTransfer(
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount,
            String idempotencyKey,
            Long actorUserId) {

        try {
            return transferService.transfer(
                    fromAccountId,
                    toAccountId,
                    amount,
                    idempotencyKey,
                    actorUserId
            );

        } catch (RuntimeException exception) {

            boolean validRequest =
                    fromAccountId != null
                            && toAccountId != null
                            && !fromAccountId.equals(toAccountId)
                            && amount != null
                            && amount.compareTo(BigDecimal.ZERO) > 0
                            && idempotencyKey != null
                            && !idempotencyKey.trim().isEmpty();

            if (validRequest) {
                try {
                    Account fromAccount = accountRepository
                            .findById(fromAccountId)
                            .orElse(null);

                    Account toAccount = accountRepository
                            .findById(toAccountId)
                            .orElse(null);

                    boolean keyAlreadyUsed =
                            transactionRepository
                                    .findByIdempotencyKey(idempotencyKey)
                                    .isPresent();

                    if (fromAccount != null
                            && toAccount != null
                            && !keyAlreadyUsed) {

                        BankTransaction failedTransaction =
                                new BankTransaction();

                        failedTransaction.setIdempotencyKey(idempotencyKey);
                        failedTransaction.setFromAccountId(fromAccountId);
                        failedTransaction.setToAccountId(toAccountId);
                        failedTransaction.setTransactionType("TRANSFER");
                        failedTransaction.setAmount(amount);
                        failedTransaction.setDescription("Account transfer");

                        failureProcessor.recordFailure(
                                failedTransaction,
                                actorUserId,
                                exception
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
