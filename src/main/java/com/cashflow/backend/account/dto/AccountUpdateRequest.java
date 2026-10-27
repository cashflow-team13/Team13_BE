
package com.cashflow.backend.account.dto;

import jakarta.validation.constraints.NotBlank;

public record AccountUpdateRequest(

        @NotBlank
        String accountName

) {
}
