
package com.cashflow.backend.transaction.dto;

import com.cashflow.backend.transaction.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        Long accountId,
        BigDecimal amount,
        String type,
        String category,
        LocalDate transactionDate
) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getTransactionDate()
        );
    }
}
