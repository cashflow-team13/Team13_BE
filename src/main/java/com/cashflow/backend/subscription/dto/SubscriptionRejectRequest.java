package com.cashflow.backend.subscription.dto;

public record SubscriptionRejectRequest(
        Long userId,
        Long accountId,
        String merchantName
) {}

//사용자는 구독 이름만 선택
//서버가 실제 탐지 결과에서 금액과 결제일을 가져옴
