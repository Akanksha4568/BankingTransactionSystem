
package com.banking.service.failure;

import com.banking.model.BankTransaction;
import org.springframework.stereotype.Service;

@Service
public class TransactionFailureProcessor {

    private final TransactionFailureService failureService;

    public TransactionFailureProcessor(
            TransactionFailureService failureService) {
        this.failureService = failureService;
    }

    public void recordFailure(BankTransaction transaction,
                              Long actorUserId,
                              RuntimeException exception) {

        String reason = exception.getMessage();

        if (reason == null || reason.trim().isEmpty()) {
            reason = "Transaction failed due to an unexpected error.";
        }

        failureService.recordFailure(
                transaction,
                actorUserId,
                reason
        );
    }
}
