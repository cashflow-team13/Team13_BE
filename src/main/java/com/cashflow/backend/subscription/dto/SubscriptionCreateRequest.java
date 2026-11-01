
package com.cashflow.backend.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionCreateRequest(
        Long userId,
        String serviceName,
        BigDecimal price,
        LocalDate nextPaymentDate,
        String billingCycle
) {}
