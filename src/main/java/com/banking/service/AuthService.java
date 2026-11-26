package com.banking.service;

import com.banking.exception.InvalidCredentialsException;
import com.banking.repository.AppUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.banking.model.AppUser;
import com.banking.model.LoginResponse;
import java.util.Optional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Long register(String username, String email, String password) {

        // 1. Validate empty fields
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        // 2. Check password length
        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        // 3. Check duplicate username
        if (appUserRepository.existsByUsername(username.trim())) {
            throw new IllegalArgumentException("Username already exists.");
        }

        // 4. Check duplicate email
        if (appUserRepository.existsByEmail(email.trim())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        // 5. Create user object
        com.banking.model.AppUser user = new com.banking.model.AppUser();

        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setActive(true);

        // 6. Save user and return generated ID
        return appUserRepository.save(user);
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
        return new LoginResponse(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                null
        );
    }

}