
package com.banking.service.history;

import com.banking.model.BankTransaction;
import com.banking.model.TransactionHistoryPageResponse;
import com.banking.model.TransactionHistoryResponse;
import com.banking.repository.BankTransactionRepository;
import org.springframework.stereotype.Service;

import com.banking.model.Customer;
import com.banking.model.Account;
import com.banking.repository.CustomerRepository;
import com.banking.repository.AccountRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionHistoryService {

    private final BankTransactionRepository bankTransactionRepository;

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public TransactionHistoryService(
            BankTransactionRepository bankTransactionRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository) {

        this.bankTransactionRepository = bankTransactionRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    public List<TransactionHistoryResponse> getTransactionHistory(
            Long accountId,
            int page,
            int size,
            String transactionType,
            String status) {

        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException("Invalid account ID");
        }

        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }

        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        int offset = page * size;

        List<BankTransaction> transactions =
                bankTransactionRepository.findTransactionsByAccountId(
                        accountId,
                        size,
                        offset,
                        transactionType,
                        status
                );

        return transactions.stream()
                .map(transaction -> new TransactionHistoryResponse(
                        transaction.getTransactionId(),
                        transaction.getFromAccountId(),
                        transaction.getToAccountId(),
                        transaction.getTransactionType(),
                        transaction.getAmount(),
                        transaction.getStatus(),
                        transaction.getDescription(),
                        transaction.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public int getTotalTransactions(
            Long accountId,
            String transactionType,
            String status) {

        return bankTransactionRepository.countTransactionsByAccountId(
                accountId,
                transactionType,
                status
        );
    }

    public TransactionHistoryPageResponse getTransactionHistoryPage(
            Long accountId,
            int page,
            int size,
            String transactionType,
            String status) {

        List<TransactionHistoryResponse> transactions =
                getTransactionHistory(
                        accountId,
                        page,
                        size,
                        transactionType,
                        status
                );

        int totalTransactions = getTotalTransactions(
                accountId,
                transactionType,
                status
        );

        return new TransactionHistoryPageResponse(
                transactions,
                page,
                size,
                totalTransactions
        );
    }

    public void validateAccountOwnership(Long userId, Long accountId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found")
                );

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found")
                );

        boolean belongsToCustomer = accountRepository.belongsToCustomer(
                account.getAccountId(),
                customer.getCustomerId()
        );

        if (!belongsToCustomer) {
            throw new IllegalArgumentException(
                    "You are not authorized to access this account"
            );
        }
    }
}
