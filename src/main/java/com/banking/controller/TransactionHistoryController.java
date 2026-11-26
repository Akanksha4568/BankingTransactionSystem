package com.banking.controller;

import com.banking.service.history.TransactionHistoryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.model.TransactionHistoryPageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.banking.service.transaction.DepositExecutionService;

import com.banking.model.DepositRequest;
import com.banking.model.BankTransaction;
import com.banking.model.TransactionHistoryResponse;
import com.banking.model.WithdrawalRequest;
import com.banking.model.TransferRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.banking.service.transaction.WithdrawalExecutionService;
import com.banking.service.transaction.TransferExecutionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionHistoryController {

    private final TransactionHistoryService transactionHistoryService;
    private final DepositExecutionService depositExecutionService;
    private final WithdrawalExecutionService withdrawalExecutionService;
    private final TransferExecutionService transferExecutionService;

    public TransactionHistoryController(
            TransactionHistoryService transactionHistoryService,
            DepositExecutionService depositExecutionService,
            WithdrawalExecutionService withdrawalExecutionService,
            TransferExecutionService transferExecutionService) {

        this.transactionHistoryService = transactionHistoryService;
        this.depositExecutionService = depositExecutionService;
        this.withdrawalExecutionService = withdrawalExecutionService;
        this.transferExecutionService = transferExecutionService;
    }

    @GetMapping("/history")
    public TransactionHistoryPageResponse getTransactionHistory(

            @RequestParam Long userId,
            @RequestParam Long accountId,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String status) {

        transactionHistoryService.validateAccountOwnership(
                userId,
                accountId
        );

        return transactionHistoryService.getTransactionHistoryPage(
                accountId,
                page,
                size,
                transactionType,
                status
        );
    }

    @PostMapping("/deposit")
    public TransactionHistoryResponse deposit(
            @RequestBody DepositRequest request) {

        // Validate account ownership
        transactionHistoryService.validateAccountOwnership(
                request.getUserId(),
                request.getAccountId()
        );

        // Execute deposit
        BankTransaction transaction =
                depositExecutionService.executeDeposit(
                        request.getAccountId(),
                        request.getAmount(),
                        request.getIdempotencyKey(),
                        request.getUserId()
                );

        // Return a safe response without exposing the idempotency key
        return new TransactionHistoryResponse(
                transaction.getTransactionId(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }

    @PostMapping("/withdraw")
    public TransactionHistoryResponse withdraw(
            @RequestBody WithdrawalRequest request) {

        // Validate account ownership
        transactionHistoryService.validateAccountOwnership(
                request.getUserId(),
                request.getAccountId()
        );

        // Execute withdrawal
        BankTransaction transaction =
                withdrawalExecutionService.executeWithdrawal(
                        request.getAccountId(),
                        request.getAmount(),
                        request.getIdempotencyKey(),
                        request.getUserId()
                );

        // Return a safe response
        return new TransactionHistoryResponse(
                transaction.getTransactionId(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }

    @PostMapping("/transfer")
    public TransactionHistoryResponse transfer(
            @RequestBody TransferRequest request) {

        transactionHistoryService.validateAccountOwnership(
                request.getUserId(),
                request.getFromAccountId()
        );

        BankTransaction transaction =
                transferExecutionService.executeTransfer(
                        request.getFromAccountId(),
                        request.getToAccountId(),
                        request.getAmount(),
                        request.getIdempotencyKey(),
                        request.getUserId()
                );

        return new TransactionHistoryResponse(
                transaction.getTransactionId(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }

}