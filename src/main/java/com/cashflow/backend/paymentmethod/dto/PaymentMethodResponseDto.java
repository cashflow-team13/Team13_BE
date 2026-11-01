
package com.cashflow.backend.paymentmethod.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentMethodResponseDto {

    private Long id;
    private Long userId;
    private String cardName;
    private String cardType;

}
