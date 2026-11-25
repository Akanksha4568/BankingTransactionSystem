
package com.banking.service.failure;

import java.util.Optional;

import com.banking.model.BankTransaction;
import com.banking.model.FailedTransaction;
import com.banking.model.TransactionAudit;
import com.banking.repository.BankTransactionRepository;
import com.banking.repository.FailedTransactionRepository;
import com.banking.repository.TransactionAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionFailureService {

    private final BankTransactionRepository transactionRepository;
    private final FailedTransactionRepository failedTransactionRepository;
    private final TransactionAuditRepository auditRepository;

    public TransactionFailureService(
            BankTransactionRepository transactionRepository,
            FailedTransactionRepository failedTransactionRepository,
            TransactionAuditRepository auditRepository) {

        this.transactionRepository = transactionRepository;
        this.failedTransactionRepository = failedTransactionRepository;
        this.auditRepository = auditRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(BankTransaction originalTransaction,
                              Long actorUserId,
                              String reason) {

        // Create a separate FAILED transaction record
        originalTransaction.setTransactionId(null);
        originalTransaction.setStatus("FAILED");

        //creates a new transaction with FAILED status
        Optional<Long> savedTransactionId =
                transactionRepository.saveIfAbsent(originalTransaction);

        if (!savedTransactionId.isPresent()) {

            BankTransaction existing =
                    transactionRepository.findByIdempotencyKey(
                            originalTransaction.getIdempotencyKey()
                    ).orElseThrow(() ->
                            new IllegalStateException(
                                    "Existing transaction could not be retrieved."
                            )
                    );

            boolean sameRequest =
                    originalTransaction.getTransactionType()
                            .equals(existing.getTransactionType())
                            && java.util.Objects.equals(
                            originalTransaction.getFromAccountId(),
                            existing.getFromAccountId()
                    )
                            && java.util.Objects.equals(
                            originalTransaction.getToAccountId(),
                            existing.getToAccountId()
                    )
                            && originalTransaction.getAmount()
                            .compareTo(existing.getAmount()) == 0;

            if (!sameRequest) {
                throw new IllegalArgumentException(
                        "Idempotency key has already been used for another transaction."
                );
            }

            return;
        }

        Long transactionId = savedTransactionId.get();

        // Save the failure reason
        FailedTransaction failedTransaction = new FailedTransaction();
        failedTransaction.setTransactionId(transactionId);
        failedTransaction.setFailureReason(reason);

        failedTransactionRepository.save(failedTransaction);  //stores the reason for failure

        // Save the audit record
        TransactionAudit audit = new TransactionAudit();
        audit.setTransactionId(transactionId);
        audit.setActorUserId(actorUserId);
        audit.setAction("TRANSACTION_FAILED");
        audit.setDetails(reason);

        auditRepository.save(audit);  //records the failure event
    }
}
