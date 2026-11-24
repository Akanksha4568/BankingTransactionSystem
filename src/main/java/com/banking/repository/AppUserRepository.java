package com.banking.repository;

import com.banking.model.AppUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AppUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public AppUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //Converts database records into Java objects

    private final RowMapper<AppUser> appUserRowMapper = (rs, rowNum) -> {
        AppUser user = new AppUser();

        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("is_active"));
        user.setCreatedAt(
                rs.getTimestamp("created_at").toLocalDateTime()
        );

        return user;
    };

    public Optional<AppUser> findByUsername(String username) {

        String sql = "SELECT * FROM app_user WHERE username = ?";

        List<AppUser> users = jdbcTemplate.query(
                sql,
                appUserRowMapper,
                username
        );

        return users.stream().findFirst();
    }

    public Optional<AppUser> findByEmail(String email) {

        String sql = "SELECT * FROM app_user WHERE email = ?";

        List<AppUser> users = jdbcTemplate.query(
                sql,
                appUserRowMapper,
                email
        );

        return users.stream().findFirst();
    }
}