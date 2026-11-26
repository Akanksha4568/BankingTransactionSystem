
package com.banking.service.transaction;

import com.banking.model.BankTransaction;
import com.banking.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest
@Transactional
class DepositServiceTest {

    @Autowired
    private DepositService depositService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "UPDATE account SET balance = ?, status = 'ACTIVE' WHERE account_id = ?",
                new BigDecimal("10000.00"),
                1L
        );
    }

    @Test
    void shouldDepositAmountSuccessfully() {

        // Deposit Rs. 500 into account ID 1.
        BankTransaction transaction = depositService.deposit(
                1L,
                new BigDecimal("500.00"),
                "TEST-DEP-001",
                1L
        );

        // Check transaction status.
        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getStatus());

        // Check updated account balance.
        BigDecimal updatedBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM account WHERE account_id = ?",
                BigDecimal.class,
                1L
        );

        assertEquals(0, new BigDecimal("10500.00").compareTo(updatedBalance));
    }
}

