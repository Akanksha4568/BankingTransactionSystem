
package com.banking.service.transaction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConcurrentWithdrawalTest {

    @Autowired
    private WithdrawalService withdrawalService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long testAccountId;

    @BeforeEach
    void createTestAccount() {
        testAccountId = jdbcTemplate.queryForObject(
                "INSERT INTO account " +
                        "(customer_id, account_number, account_type, balance, currency, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?) RETURNING account_id",
                Long.class,
                1L,
                "C" + System.nanoTime(),
                "SAVINGS",
                new BigDecimal("10000.00"),
                "INR",
                "ACTIVE"
        );
    }

    @AfterEach
    void cleanUp() {
        if (testAccountId != null) {
            jdbcTemplate.update(
                    "DELETE FROM transaction_audit " +
                            "WHERE transaction_id IN " +
                            "(SELECT transaction_id FROM bank_transaction " +
                            "WHERE from_account_id = ? OR to_account_id = ?)",
                    testAccountId, testAccountId
            );

            jdbcTemplate.update(
                    "DELETE FROM bank_transaction " +
                            "WHERE from_account_id = ? OR to_account_id = ?",
                    testAccountId, testAccountId
            );

            jdbcTemplate.update(
                    "DELETE FROM account WHERE account_id = ?",
                    testAccountId
            );
        }
    }

    @Test
    void shouldPreventNegativeBalanceDuringConcurrentWithdrawals()
            throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startSignal = new CountDownLatch(1);

        try {
            Future<Boolean> withdrawalOne = executor.submit(() -> {
                startSignal.await();

                try {
                    withdrawalService.withdraw(
                            testAccountId,
                            new BigDecimal("7000.00"),
                            "CONCURRENT-WITH-7000-" + testAccountId,
                            1L
                    );
                    return true;
                } catch (RuntimeException exception) {
                    return false;
                }
            });

            Future<Boolean> withdrawalTwo = executor.submit(() -> {
                startSignal.await();

                try {
                    withdrawalService.withdraw(
                            testAccountId,
                            new BigDecimal("5000.00"),
                            "CONCURRENT-WITH-5000-" + testAccountId,
                            1L
                    );
                    return true;
                } catch (RuntimeException exception) {
                    return false;
                }
            });

            startSignal.countDown();

            boolean firstSucceeded = withdrawalOne.get();
            boolean secondSucceeded = withdrawalTwo.get();

            int successCount = 0;

            if (firstSucceeded) {
                successCount++;
            }

            if (secondSucceeded) {
                successCount++;
            }

            BigDecimal finalBalance = jdbcTemplate.queryForObject(
                    "SELECT balance FROM account WHERE account_id = ?",
                    BigDecimal.class,
                    testAccountId
            );

            assertEquals(1, successCount);
            assertTrue(finalBalance.compareTo(BigDecimal.ZERO) >= 0);

            assertTrue(
                    finalBalance.compareTo(new BigDecimal("3000.00")) == 0
                            || finalBalance.compareTo(new BigDecimal("5000.00")) == 0
            );

        } finally {
            executor.shutdownNow();
        }
    }
}
