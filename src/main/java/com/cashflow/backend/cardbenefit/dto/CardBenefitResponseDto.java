
package com.cashflow.backend.cardbenefit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CardBenefitResponseDto {

    private Long id;
    private Long paymentMethodId;
    private String category;
    private String benefitType;
    private BigDecimal benefitValue;

}
