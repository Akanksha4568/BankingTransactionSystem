
package com.banking.controller;

import com.banking.model.Account;
import com.banking.model.AppUser;
import com.banking.repository.AppUserRepository;
import com.banking.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.banking.model.CreateAccountRequest;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AppUserRepository appUserRepository;

    public AccountController(
            AccountService accountService,
            AppUserRepository appUserRepository) {

        this.accountService = accountService;
        this.appUserRepository = appUserRepository;
    }

    @PostMapping("/create")
    public Account createAccount(
            @RequestBody CreateAccountRequest request,
            Authentication authentication) {

        // Get currently logged-in user's username
        String username = authentication.getName();

        // Find the user in database
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found."
                ));

        // Create account for authenticated user
        return accountService.createAccountForUser(
                user.getUserId(),
                request.getAccountType()
        );
    }

    // Get accounts of the currently logged-in user
    @GetMapping("/my-accounts")
    public List<Account> getMyAccounts(Authentication authentication) {

        String username = authentication.getName();

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found."
                ));

        return accountService.getAccountsByUserId(user.getUserId());
    }
}
