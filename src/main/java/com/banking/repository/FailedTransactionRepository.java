package com.banking.repository;

import com.banking.model.FailedTransaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FailedTransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public FailedTransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<FailedTransaction> failedTransactionRowMapper =
            (rs, rowNum) -> {

                FailedTransaction failedTransaction = new FailedTransaction();

                failedTransaction.setFailureId(
                        rs.getLong("failure_id")
                );
                failedTransaction.setTransactionId(
                        rs.getLong("transaction_id")
                );
                failedTransaction.setFailureReason(
                        rs.getString("failure_reason")
                );
                failedTransaction.setFailedAt(
                        rs.getTimestamp("failed_at").toLocalDateTime()
                );

                return failedTransaction;
            };

    public List<FailedTransaction> findAll() {

        String sql = "SELECT * FROM failed_transaction ORDER BY failed_at DESC";

        return jdbcTemplate.query(
                sql,
                failedTransactionRowMapper
        );
    }

    public List<FailedTransaction> findByTransactionId(Long transactionId) {

        String sql = "SELECT * FROM failed_transaction WHERE transaction_id = ?";

        return jdbcTemplate.query(
                sql,
                failedTransactionRowMapper,
                transactionId
        );
    }

    public int save(FailedTransaction failedTransaction) {

        String sql = "INSERT INTO failed_transaction " +
                "(transaction_id, failure_reason) " +
                "VALUES (?, ?)";

        return jdbcTemplate.update(
                sql,
                failedTransaction.getTransactionId(),
                failedTransaction.getFailureReason()
        );
    }
}