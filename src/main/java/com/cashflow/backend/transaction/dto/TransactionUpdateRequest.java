
package com.cashflow.backend.transaction.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionUpdateRequest(
        @NotNull @DecimalMin("0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount,
        @NotBlank @Pattern(regexp = "INCOME|EXPENSE")
        String type,
        String category,
        String merchantName,
        @NotNull LocalDate transactionDate
) {
}
