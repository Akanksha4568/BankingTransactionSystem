
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
class TransferExecutionServiceTest {

    @Autowired
    private TransferExecutionService transferExecutionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long fromAccountId;
    private Long toAccountId;
    private String idempotencyKey;

    @BeforeEach
    void setUp() {

        idempotencyKey = "TFAIL-" + UUID.randomUUID();

        String fromAccountNumber =
                "T" + UUID.randomUUID().toString()
                        .replace("-", "").substring(0, 15);

        String toAccountNumber =
                "T" + UUID.randomUUID().toString()
                        .replace("-", "").substring(0, 15);

        fromAccountId = jdbcTemplate.queryForObject(
                "INSERT INTO account " +
                        "(customer_id, account_number, account_type, " +
                        "balance, currency, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?) " +
                        "RETURNING account_id",
                Long.class,
                1L,
                fromAccountNumber,
                "SAVINGS",
                new BigDecimal("10000.00"),
                "INR",
                "FROZEN"
        );

        toAccountId = jdbcTemplate.queryForObject(
                "INSERT INTO account " +
                        "(customer_id, account_number, account_type, " +
                        "balance, currency, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?) " +
                        "RETURNING account_id",
                Long.class,
                1L,
                toAccountNumber,
                "SAVINGS",
                new BigDecimal("5000.00"),
                "INR",
                "ACTIVE"
        );
    }

    @Test
    void shouldRecordFailedTransferWithoutChangingBalances() {

        assertThrows(IllegalArgumentException.class, () ->
                transferExecutionService.executeTransfer(
                        fromAccountId,
                        toAccountId,
                        new BigDecimal("500.00"),
                        idempotencyKey,
                        1L
                )
        );

        BigDecimal fromBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM account WHERE account_id = ?",
                BigDecimal.class,
                fromAccountId
        );

        BigDecimal toBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM account WHERE account_id = ?",
                BigDecimal.class,
                toAccountId
        );

        assertEquals(new BigDecimal("10000.00"), fromBalance);
        assertEquals(new BigDecimal("5000.00"), toBalance);

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

        if (fromAccountId != null) {
            jdbcTemplate.update(
                    "DELETE FROM account WHERE account_id = ?",
                    fromAccountId
            );
        }

        if (toAccountId != null) {
            jdbcTemplate.update(
                    "DELETE FROM account WHERE account_id = ?",
                    toAccountId
            );
        }
    }
}
