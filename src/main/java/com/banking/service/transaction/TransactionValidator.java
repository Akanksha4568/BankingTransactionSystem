
package com.banking.service.transaction;

import com.banking.model.Account;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class TransactionValidator {

    //Rejects zero, negative or null amounts
    public void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero."
            );
        }
    }

    //	Rejects missing or inactive accounts
    public void validateAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account does not exist.");
        }

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new IllegalArgumentException("Account is not active.");
        }
    }

    //Prevents withdrawals exceeding the balance
    public void validateSufficientBalance(Account account, BigDecimal amount) {
        validateAccount(account);
        validateAmount(amount);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient account balance.");
        }
    }

    //Prevents transfers to the same account
    public void validateTransferAccounts(Account fromAccount, Account toAccount) {
        validateAccount(fromAccount);
        validateAccount(toAccount);

        if (Objects.equals(fromAccount.getAccountId(), toAccount.getAccountId())) {
            throw new IllegalArgumentException(
                    "Cannot transfer money to the same account."
            );
        }
    }
}
