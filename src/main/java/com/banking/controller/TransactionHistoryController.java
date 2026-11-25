package com.banking.controller;

import com.banking.service.history.TransactionHistoryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.model.TransactionHistoryPageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/transactions")
public class TransactionHistoryController {

    private final TransactionHistoryService transactionHistoryService;

    public TransactionHistoryController(
            TransactionHistoryService transactionHistoryService) {

        this.transactionHistoryService = transactionHistoryService;
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
}