package com.banking.repository;

import com.banking.model.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;  //Converts database rows into Java Account objects
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;  //	Handles cases where an account is not found

@Repository  //	Identifies this class as a database component
public class AccountRepository {

    private final JdbcTemplate jdbcTemplate;  //	Executes SQL queries

    public AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Account> accountRowMapper = (rs, rowNum) -> {
        Account account = new Account();

        account.setAccountId(rs.getLong("account_id"));
        account.setCustomerId(rs.getLong("customer_id"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setAccountType(rs.getString("account_type"));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setCurrency(rs.getString("currency"));
        account.setStatus(rs.getString("status"));
        account.setCreatedAt(
                rs.getTimestamp("created_at").toLocalDateTime()
        );

        return account;
    };

    public Optional<Account> findById(Long accountId) {
        // using parameterized SQL (?) instead of directly inserting values into SQL strings this helps prevent SQL injection
        String sql = "SELECT * FROM account WHERE account_id = ?";

        List<Account> accounts = jdbcTemplate.query(
                sql,
                accountRowMapper,
                accountId
        );

        return accounts.stream().findFirst();
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {

        String sql = "SELECT * FROM account WHERE account_number = ?";

        List<Account> accounts = jdbcTemplate.query(
                sql,
                accountRowMapper,
                accountNumber
        );

        return accounts.stream().findFirst();
    }
}