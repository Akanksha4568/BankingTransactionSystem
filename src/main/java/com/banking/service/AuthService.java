package com.banking.service;

import com.banking.exception.InvalidCredentialsException;
import com.banking.repository.AppUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.banking.model.AppUser;
import com.banking.model.LoginResponse;
import java.util.Optional;
import com.banking.model.Customer;
import com.banking.repository.CustomerRepository;
import org.springframework.transaction.annotation.Transactional;
import com.banking.repository.AccountRepository;
import com.banking.model.Account;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public AuthService(
            AppUserRepository appUserRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            JwtService jwtService) {

        this.appUserRepository = appUserRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtService = jwtService;
    }

    @Transactional
    public Long register(
            String username,
            String email,
            String password,
            String fullName,
            String phone,
            String address) {

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address is required.");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        if (appUserRepository.existsByUsername(username.trim())) {
            throw new IllegalArgumentException("Username already exists.");
        }

        if (appUserRepository.existsByEmail(email.trim())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        AppUser user = new AppUser();

        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setActive(true);

        Long userId = appUserRepository.save(user);

        Customer customer = new Customer();

        customer.setUserId(userId);
        customer.setFullName(fullName.trim());
        customer.setPhone(phone.trim());
        customer.setAddress(address.trim());

        Customer savedCustomer = customerRepository.save(customer);

        Account account = new Account();

        account.setCustomerId(savedCustomer.getCustomerId());
        account.setAccountNumber("BANK" + System.currentTimeMillis());
        account.setAccountType("SAVINGS");
        account.setBalance(new java.math.BigDecimal("0.00"));
        account.setCurrency("INR");
        account.setStatus("ACTIVE");

        accountRepository.save(account);

        return userId;
    }


    public LoginResponse login(String username, String password) {

        // 1. Validate empty fields
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        // 2. Find user by username
        Optional<AppUser> optionalUser =
                appUserRepository.findByUsername(username.trim());

        if (!optionalUser.isPresent()) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        AppUser user = optionalUser.get();

        // 3. Check whether account is active
        if (!user.isActive()) {
            throw new IllegalArgumentException("Your account is inactive.");
        }

        // 4. Verify password using BCrypt
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        // 5. Return login response
        String token = jwtService.generateToken(user);
        return new LoginResponse(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                token
        );
    }

}