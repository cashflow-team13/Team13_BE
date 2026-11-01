
package com.cashflow.backend.paymentmethod.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class PaymentMethodRequestDto {

    @NotNull(message = "사용자 ID는 필수 입력 항목입니다.")
    private Long userId;

    @NotBlank(message = "카드 이름은 필수 입력 항목입니다.")
    private String cardName;

    @NotBlank(message = "카드 종류는 필수 입력 항목입니다.")
    private String cardType;

}
