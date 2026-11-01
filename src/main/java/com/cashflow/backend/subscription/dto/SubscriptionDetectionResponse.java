package com.cashflow.backend.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionDetectionResponse(
        String merchantName,
        BigDecimal averageAmount,
        int paymentCount, //반복 횟수
        long averageIntervalDays, //평균 결제 간격
        LocalDate lastPaymentDate, //마지막 결제일
        LocalDate nextExpectedPaymentDate //다음 예상 결제일
) {
}
