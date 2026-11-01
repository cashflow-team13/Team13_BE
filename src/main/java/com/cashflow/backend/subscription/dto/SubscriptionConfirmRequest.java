
package com.cashflow.backend.subscription.dto;

public record SubscriptionConfirmRequest(
        Long userId,
        Long accountId,
        String merchantName
) {}
