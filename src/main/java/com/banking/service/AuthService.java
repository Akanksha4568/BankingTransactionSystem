package com.banking.service;

import com.banking.repository.AppUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
}