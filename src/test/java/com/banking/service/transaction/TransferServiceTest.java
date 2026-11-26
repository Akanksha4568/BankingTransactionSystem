package com.banking.service.transaction;

import com.banking.model.BankTransaction;
import com.banking.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest
@Transactional
class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "UPDATE account SET balance = ?, status = 'ACTIVE', currency = 'INR' WHERE account_id = ?",
                new BigDecimal("10000.00"),
                1L
        );
    }

    @Test
    void shouldTransferMoneySuccessfully() {

        Long receiverAccountId = jdbcTemplate.queryForObject(
                "INSERT INTO account " +
                        "(customer_id, account_number, account_type, balance, currency, status) " +
                        "VALUES (1, ?, 'SAVINGS', 0.00, 'INR', 'ACTIVE') " +
                        "RETURNING account_id",
                Long.class,
                "T" + System.nanoTime()
        );

        BankTransaction transaction = transferService.transfer(
                1L,
                receiverAccountId,
                new BigDecimal("1000.00"),
                "TRANSFER-TEST-001",
                1L
        );

        assertEquals("TRANSFER", transaction.getTransactionType());
        assertEquals("SUCCESS", transaction.getStatus());
        assertEquals(new BigDecimal("1000.00"), transaction.getAmount());

        BigDecimal senderBalance =
                accountRepository.findById(1L).get().getBalance();

        BigDecimal receiverBalance =
                accountRepository.findById(receiverAccountId).get().getBalance();

        assertEquals(new BigDecimal("9000.00"), senderBalance);
        assertEquals(new BigDecimal("1000.00"), receiverBalance);
    }

    @Test
    void shouldRejectTransferWhenBalanceIsInsufficient() {

        Long receiverAccountId = jdbcTemplate.queryForObject(
                "INSERT INTO account " +
                        "(customer_id, account_number, account_type, balance, currency, status) " +
                        "VALUES (1, ?, 'SAVINGS', 0.00, 'INR', 'ACTIVE') " +
                        "RETURNING account_id",
                Long.class,
                "T" + System.nanoTime()
        );

        assertThrows(IllegalArgumentException.class, () ->
                transferService.transfer(
                        1L,
                        receiverAccountId,
                        new BigDecimal("20000.00"),
                        "TRANSFER-TEST-002",
                        1L
                )
        );

        BigDecimal senderBalance =
                accountRepository.findById(1L).get().getBalance();

        BigDecimal receiverBalance =
                accountRepository.findById(receiverAccountId).get().getBalance();

        assertEquals(new BigDecimal("10000.00"), senderBalance);
        assertEquals(new BigDecimal("0.00"), receiverBalance);
    }

    @Test
    void shouldRejectTransferToSameAccount() {

        assertThrows(IllegalArgumentException.class, () ->
                transferService.transfer(
                        1L,
                        1L,
                        new BigDecimal("500.00"),
                        "TRANSFER-TEST-003",
                        1L
                )
        );

        BigDecimal balance =
                accountRepository.findById(1L).get().getBalance();

        assertEquals(new BigDecimal("10000.00"), balance);
    }

    @Test
    void shouldNotProcessSameTransferTwice() {

        Long receiverAccountId = jdbcTemplate.queryForObject(
                "INSERT INTO account " +
                        "(customer_id, account_number, account_type, balance, currency, status) " +
                        "VALUES (1, ?, 'SAVINGS', 0.00, 'INR', 'ACTIVE') " +
                        "RETURNING account_id",
                Long.class,
                "T" + System.nanoTime()
        );

        BankTransaction firstTransaction = transferService.transfer(
                1L,
                receiverAccountId,
                new BigDecimal("1000.00"),
                "TRANSFER-TEST-004",
                1L
        );

        BankTransaction secondTransaction = transferService.transfer(
                1L,
                receiverAccountId,
                new BigDecimal("1000.00"),
                "TRANSFER-TEST-004",
                1L
        );

        assertEquals(
                firstTransaction.getTransactionId(),
                secondTransaction.getTransactionId()
        );

        BigDecimal senderBalance =
                accountRepository.findById(1L).get().getBalance();

        BigDecimal receiverBalance =
                accountRepository.findById(receiverAccountId).get().getBalance();

        assertEquals(new BigDecimal("9000.00"), senderBalance);
        assertEquals(new BigDecimal("1000.00"), receiverBalance);
    }

    @Test
    void shouldRejectSameKeyWithDifferentTransferAmount() {

        Long receiverAccountId = jdbcTemplate.queryForObject(
                "INSERT INTO account " +
                        "(customer_id, account_number, account_type, balance, currency, status) " +
                        "VALUES (1, ?, 'SAVINGS', 0.00, 'INR', 'ACTIVE') " +
                        "RETURNING account_id",
                Long.class,
                "T" + System.nanoTime()
        );

        transferService.transfer(
                1L,
                receiverAccountId,
                new BigDecimal("1000.00"),
                "TRANSFER-TEST-005",
                1L
        );

        assertThrows(IllegalArgumentException.class, () ->
                transferService.transfer(
                        1L,
                        receiverAccountId,
                        new BigDecimal("2000.00"),
                        "TRANSFER-TEST-005",
                        1L
                )
        );

        BigDecimal senderBalance =
                accountRepository.findById(1L).get().getBalance();

        assertEquals(new BigDecimal("9000.00"), senderBalance);
    }

    @Test
    void shouldRejectTransferBetweenDifferentCurrencies() {

        Long receiverAccountId = jdbcTemplate.queryForObject(
                "INSERT INTO account " +
                        "(customer_id, account_number, account_type, balance, currency, status) " +
                        "VALUES (1, ?, 'SAVINGS', 0.00, 'USD', 'ACTIVE') " +
                        "RETURNING account_id",
                Long.class,
                "CUR" + System.nanoTime()
        );

        assertThrows(IllegalArgumentException.class, () ->
                transferService.transfer(
                        1L,
                        receiverAccountId,
                        new BigDecimal("1000.00"),
                        "TRANSFER-CURRENCY-TEST",
                        1L
                )
        );

        assertEquals(
                new BigDecimal("10000.00"),
                accountRepository.findById(1L)
                        .orElseThrow(() -> new IllegalStateException("Account not found."))
                        .getBalance()
        );
    }
}
