
package com.banking.service.transaction;

import com.banking.model.BankTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class WithdrawalServiceTest {

    @Autowired
    private WithdrawalService withdrawalService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldWithdrawAmountSuccessfully() {

        BankTransaction transaction = withdrawalService.withdraw(
                1L,
                new BigDecimal("500.00"),
                "TEST-WITH-001",
                1L
        );

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getStatus());

        BigDecimal updatedBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM account WHERE account_id = ?",
                BigDecimal.class,
                1L
        );

        assertEquals(
                0,
                new BigDecimal("9500.00").compareTo(updatedBalance)
        );
    }


    @Test
    void shouldNotProcessSameWithdrawalTwice() {

        BankTransaction firstTransaction = withdrawalService.withdraw(
                1L,
                new BigDecimal("500.00"),
                "TEST-DUP-WITH-001",
                1L
        );

        BankTransaction secondTransaction = withdrawalService.withdraw(
                1L,
                new BigDecimal("500.00"),
                "TEST-DUP-WITH-001",
                1L
        );

        assertEquals(
                firstTransaction.getTransactionId(),
                secondTransaction.getTransactionId()
        );

        BigDecimal updatedBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM account WHERE account_id = ?",
                BigDecimal.class,
                1L
        );

        assertEquals(
                0,
                new BigDecimal("9500.00").compareTo(updatedBalance)
        );
    }


    @Test
    void shouldRejectSameKeyWithDifferentAmount() {

        withdrawalService.withdraw(
                1L,
                new BigDecimal("500.00"),
                "TEST-KEY-MISMATCH-001",
                1L
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> withdrawalService.withdraw(
                        1L,
                        new BigDecimal("1000.00"),
                        "TEST-KEY-MISMATCH-001",
                        1L
                )
        );
    }


    @Test
    void shouldRejectWithdrawalWhenBalanceIsInsufficient() {

        assertThrows(
                IllegalArgumentException.class,
                () -> withdrawalService.withdraw(
                        1L,
                        new BigDecimal("15000.00"),
                        "TEST-INSUFFICIENT-001",
                        1L
                )
        );

        BigDecimal updatedBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM account WHERE account_id = ?",
                BigDecimal.class,
                1L
        );

        assertEquals(
                0,
                new BigDecimal("10000.00").compareTo(updatedBalance)
        );
    }


    @Test
    void shouldRejectWithdrawalFromInactiveAccount() {

        jdbcTemplate.update(
                "UPDATE account SET status = 'FROZEN' WHERE account_id = ?",
                1L
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> withdrawalService.withdraw(
                        1L,
                        new BigDecimal("500.00"),
                        "TEST-FROZEN-001",
                        1L
                )
        );

        BigDecimal balance = jdbcTemplate.queryForObject(
                "SELECT balance FROM account WHERE account_id = ?",
                BigDecimal.class,
                1L
        );

        assertEquals(
                0,
                new BigDecimal("10000.00").compareTo(balance)
        );
    }


}
