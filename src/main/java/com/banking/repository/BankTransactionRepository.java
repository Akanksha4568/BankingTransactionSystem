package com.banking.repository;

import com.banking.model.BankTransaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BankTransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public BankTransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<BankTransaction> transactionRowMapper =
            (rs, rowNum) -> {

                BankTransaction transaction = new BankTransaction();

                transaction.setTransactionId(
                        rs.getLong("transaction_id")
                );
                transaction.setIdempotencyKey(
                        rs.getString("idempotency_key")
                );
                transaction.setFromAccountId(
                        rs.getObject("from_account_id", Long.class)
                );
                transaction.setToAccountId(
                        rs.getObject("to_account_id", Long.class)
                );
                transaction.setTransactionType(
                        rs.getString("transaction_type")
                );
                transaction.setAmount(
                        rs.getBigDecimal("amount")
                );
                transaction.setStatus(
                        rs.getString("status")
                );
                transaction.setDescription(
                        rs.getString("description")
                );
                transaction.setCreatedAt(
                        rs.getTimestamp("created_at").toLocalDateTime()
                );

                return transaction;
            };

    public Optional<BankTransaction> findById(Long transactionId) {

        String sql = " SELECT * FROM bank_transaction WHERE transaction_id = ?";

        List<BankTransaction> transactions = jdbcTemplate.query(
                sql,
                transactionRowMapper,
                transactionId
        );

        return transactions.stream().findFirst();
    }

    public Optional<BankTransaction> findByIdempotencyKey(
            String idempotencyKey) {

        String sql = "SELECT * FROM bank_transaction WHERE idempotency_key = ?";

        List<BankTransaction> transactions = jdbcTemplate.query(
                sql,
                transactionRowMapper,
                idempotencyKey
        );

        return transactions.stream().findFirst();
    }


    //save customer transaction details
    public Long save(BankTransaction transaction) {

        String sql = "INSERT INTO bank_transaction " +
                "(idempotency_key, from_account_id, to_account_id, " +
                "transaction_type, amount, status, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "RETURNING transaction_id";

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                transaction.getIdempotencyKey(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getDescription()
        );
    }

    public int updateStatus(Long transactionId, String status) {

        String sql = "UPDATE bank_transaction " +
                "SET status = ? " +
                "WHERE transaction_id = ? " +
                "AND status = 'PENDING'";

        return jdbcTemplate.update(sql, status, transactionId);
    }
}