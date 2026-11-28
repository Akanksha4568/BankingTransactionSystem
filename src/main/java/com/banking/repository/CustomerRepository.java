package com.banking.repository;

import com.banking.model.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomerRepository {

    private final JdbcTemplate jdbcTemplate;

    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //	Converts database records into Customer objects

    private final RowMapper<Customer> customerRowMapper = (rs, rowNum) -> {
        Customer customer = new Customer();

        customer.setCustomerId(rs.getLong("customer_id"));
        customer.setUserId(rs.getObject("user_id", Long.class));
        customer.setFullName(rs.getString("full_name"));
        customer.setPhone(rs.getString("phone"));
        customer.setAddress(rs.getString("address"));
        customer.setCreatedAt(
                rs.getTimestamp("created_at").toLocalDateTime()
        );

        return customer;
    };

    public Optional<Customer> findById(Long customerId) {

        String sql = "SELECT * FROM customer WHERE customer_id = ?";

        List<Customer> customers = jdbcTemplate.query(
                sql,
                customerRowMapper,
                customerId
        );

        return customers.stream().findFirst();
    }

    public Optional<Customer> findByUserId(Long userId) {

        String sql = "SELECT * FROM customer WHERE user_id = ?";

        List<Customer> customers = jdbcTemplate.query(
                sql,
                customerRowMapper,
                userId
        );

        return customers.stream().findFirst();
    }

    public Customer save(Customer customer) {

        String sql = "INSERT INTO customer (user_id, full_name, phone, address) VALUES (?, ?, ?, ?) RETURNING customer_id";

        Long customerId = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                customer.getUserId(),
                customer.getFullName(),
                customer.getPhone(),
                customer.getAddress()
        );

        customer.setCustomerId(customerId);

        return customer;
    }

    public List<Customer> findAllCustomers() {

        String sql = "SELECT c.* FROM customer c INNER JOIN app_user u ON c.user_id = u.user_id WHERE u.role = 'USER' ORDER BY c.customer_id ";

        return jdbcTemplate.query(
                sql,
                customerRowMapper
        );
    }

}