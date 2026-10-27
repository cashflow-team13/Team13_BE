
package com.cashflow.backend.account.dto;

import com.cashflow.backend.account.Account;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        Long userId,
        String accountName,
        BigDecimal balance
) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUser().getId(),
                account.getAccountName(),
                account.getBalance()
        );
    }
}
