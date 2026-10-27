
package com.cashflow.backend.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MockTransactionResponse(
        BigDecimal amount,
        String type,
        String category,
        String merchantName,
        LocalDate transactionDate
) {
}
