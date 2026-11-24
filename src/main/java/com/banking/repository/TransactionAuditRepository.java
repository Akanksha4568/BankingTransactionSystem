package com.banking.repository;

import com.banking.model.TransactionAudit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TransactionAuditRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionAuditRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<TransactionAudit> auditRowMapper = (rs, rowNum) -> {
        TransactionAudit audit = new TransactionAudit();

        audit.setAuditId(rs.getLong("audit_id"));
        audit.setTransactionId(
                rs.getObject("transaction_id", Long.class)
        );
        audit.setActorUserId(
                rs.getObject("actor_user_id", Long.class)
        );
        audit.setAction(rs.getString("action"));
        audit.setDetails(rs.getString("details"));
        audit.setCreatedAt(
                rs.getTimestamp("created_at").toLocalDateTime()
        );

        return audit;
    };

    public List<TransactionAudit> findByTransactionId(Long transactionId) {

        String sql = "SELECT * FROM transaction_audit " +
                "WHERE transaction_id = ? ORDER BY created_at DESC";

        return jdbcTemplate.query(
                sql,
                auditRowMapper,
                transactionId
        );
    }
}