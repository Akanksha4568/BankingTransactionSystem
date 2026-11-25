
package com.banking.service.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class DepositExecutionServiceTest {

    @Autowired
    private DepositExecutionService depositExecutionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long accountId;
    private String idempotencyKey;

    @BeforeEach
    void setUp() {

        idempotencyKey = "FAIL-" + UUID.randomUUID();

        String accountNumber =
                "F" + UUID.randomUUID().toString()
                        .replace("-", "").substring(0, 15);

        accountId = jdbcTemplate.queryForObject(
                "INSERT INTO account " +
                        "(customer_id, account_number, account_type, " +
                        "balance, currency, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?) " +
                        "RETURNING account_id",
                Long.class,
                1L,
                accountNumber,
                "SAVINGS",
                new BigDecimal("10000.00"),
                "INR",
                "FROZEN"
        );
    }

    @Test
    void shouldRecordFailedDepositWithoutChangingBalance() {

        assertThrows(IllegalArgumentException.class, () ->
                depositExecutionService.executeDeposit(
                        accountId,
                        new BigDecimal("500.00"),
                        idempotencyKey,
                        1L
                )
        );

        BigDecimal balance = jdbcTemplate.queryForObject(
                "SELECT balance FROM account WHERE account_id = ?",
                BigDecimal.class,
                accountId
        );

        assertEquals(new BigDecimal("10000.00"), balance);

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM bank_transaction " +
                        "WHERE idempotency_key = ?",
                String.class,
                idempotencyKey
        );

        assertEquals("FAILED", status);

        Integer failureCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM failed_transaction " +
                        "WHERE transaction_id = (" +
                        "SELECT transaction_id FROM bank_transaction " +
                        "WHERE idempotency_key = ?)",
                Integer.class,
                idempotencyKey
        );

        assertEquals(1, failureCount);

        Integer auditCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transaction_audit " +
                        "WHERE transaction_id = (" +
                        "SELECT transaction_id FROM bank_transaction " +
                        "WHERE idempotency_key = ?) " +
                        "AND action = 'TRANSACTION_FAILED'",
                Integer.class,
                idempotencyKey
        );

        assertEquals(1, auditCount);
    }

    @AfterEach
    void cleanUp() {

        if (idempotencyKey != null) {

            jdbcTemplate.update(
                    "DELETE FROM transaction_audit " +
                            "WHERE transaction_id IN (" +
                            "SELECT transaction_id FROM bank_transaction " +
                            "WHERE idempotency_key = ?)",
                    idempotencyKey
            );

            jdbcTemplate.update(
                    "DELETE FROM failed_transaction " +
                            "WHERE transaction_id IN (" +
                            "SELECT transaction_id FROM bank_transaction " +
                            "WHERE idempotency_key = ?)",
                    idempotencyKey
            );

            jdbcTemplate.update(
                    "DELETE FROM bank_transaction WHERE idempotency_key = ?",
                    idempotencyKey
            );
        }

        if (accountId != null) {
            jdbcTemplate.update(
                    "DELETE FROM account WHERE account_id = ?",
                    accountId
            );
        }
    }
}
