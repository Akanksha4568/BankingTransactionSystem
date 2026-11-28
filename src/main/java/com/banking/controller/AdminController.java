
package com.banking.controller;

import com.banking.model.Account;
import com.banking.model.Customer;
import com.banking.model.FailedTransaction;
import com.banking.repository.FailedTransactionRepository;
import com.banking.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.banking.model.BankTransaction;
import com.banking.repository.BankTransactionRepository;
import com.banking.model.TransactionAudit;
import com.banking.repository.TransactionAuditRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AccountService accountService;
    private final FailedTransactionRepository failedTransactionRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final TransactionAuditRepository transactionAuditRepository;
    private final JdbcTemplate jdbcTemplate;

    public AdminController(
            AccountService accountService,
            FailedTransactionRepository failedTransactionRepository,
            BankTransactionRepository bankTransactionRepository,
            TransactionAuditRepository transactionAuditRepository,
            JdbcTemplate jdbcTemplate) {

        this.accountService = accountService;
        this.failedTransactionRepository = failedTransactionRepository;
        this.bankTransactionRepository = bankTransactionRepository;
        this.transactionAuditRepository = transactionAuditRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/accounts")
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/customers")
    public List<Customer> getAllCustomers() {
        return accountService.getAllCustomers();
    }

    @GetMapping("/failed-transactions")
    public List<FailedTransaction> getAllFailedTransactions() {
        return failedTransactionRepository.findAll();
    }

    @GetMapping("/transactions")
    public List<BankTransaction> getAllTransactions() {
        return bankTransactionRepository.findAllTransactions();
    }

    @GetMapping("/audit-logs")
    public List<TransactionAudit> getAllAuditLogs() {
        return transactionAuditRepository.findAll();
    }

    @GetMapping("/statistics")
    public Map<String, Object> getDashboardStatistics() {

        Map<String, Object> statistics = new java.util.LinkedHashMap<>();

        statistics.put(
                "totalCustomers",
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM customer c " +
                                "INNER JOIN app_user u ON c.user_id = u.user_id " +
                                "WHERE u.role = 'USER'",
                        Long.class
                )
        );

        statistics.put(
                "totalAccounts",
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM account",
                        Long.class
                )
        );

        statistics.put(
                "totalTransactions",
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM bank_transaction",
                        Long.class
                )
        );

        statistics.put(
                "successfulTransactions",
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM bank_transaction WHERE status = 'SUCCESS'",
                        Long.class
                )
        );

        statistics.put(
                "failedTransactions",
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM bank_transaction WHERE status = 'FAILED'",
                        Long.class
                )
        );

        statistics.put(
                "totalBalance",
                jdbcTemplate.queryForObject(
                        "SELECT COALESCE(SUM(balance), 0) FROM account",
                        java.math.BigDecimal.class
                )
        );

        return statistics;
    }
}
