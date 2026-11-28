
package com.banking.service;

import com.banking.model.Account;
import com.banking.model.Customer;
import com.banking.repository.AccountRepository;
import com.banking.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public Optional<Account> getAccountById(Long accountId) {
        return accountRepository.findById(accountId);
    }

    public Optional<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    // Get all accounts belonging to a user
    public List<Account> getAccountsByUserId(Long userId) {

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer profile not found."));

        return accountRepository.findAllByCustomerId(
                customer.getCustomerId()
        );
    }


    @Transactional
    public Account createAccountForUser(Long userId, String accountType) {

        // Validate account type
        if (accountType == null ||
                (!accountType.equalsIgnoreCase("SAVINGS")
                        && !accountType.equalsIgnoreCase("CURRENT"))) {

            throw new IllegalArgumentException(
                    "Account type must be SAVINGS or CURRENT."
            );
        }

        // Find customer associated with logged-in user
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer profile not found.")
                );

        // Create new account
        Account account = new Account();

        account.setCustomerId(customer.getCustomerId());
        account.setAccountNumber("BANK" + System.currentTimeMillis());
        account.setAccountType(accountType.toUpperCase());
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency("INR");
        account.setStatus("ACTIVE");

        // Save account in database
        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAllAccounts();
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAllCustomers();
    }


}
