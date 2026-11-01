
package com.cashflow.backend.cardbenefit.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CardBenefitRequestDto {

    @NotNull(message = "결제수단 ID는 필수 입력 항목입니다.")
    private Long paymentMethodId;

    @NotBlank(message = "혜택 카테고리는 필수 입력 항목입니다.")
    private String category;

    @NotBlank(message = "혜택 유형은 필수 입력 항목입니다.")
    private String benefitType;

    @NotNull(message = "혜택 값은 필수 입력 항목입니다.")
    private BigDecimal benefitValue;
}
