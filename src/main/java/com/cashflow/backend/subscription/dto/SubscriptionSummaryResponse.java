package com.cashflow.backend.subscription.dto;

import java.math.BigDecimal;

public record SubscriptionSummaryResponse(
        Long userId,
        int subscriptionCount,
        BigDecimal monthlyTotal
) {}
