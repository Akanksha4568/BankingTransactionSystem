package com.banking.controller;

import com.banking.model.RegistrationRequest;
import com.banking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.banking.model.LoginRequest;
import com.banking.model.LoginResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegistrationRequest request) {

        Long userId = authService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        return "Registration successful. User ID: " + userId;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        return authService.login(
                request.getUsername(),
                request.getPassword()
        );
    }
}