
package com.cashflow.backend.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.cashflow.backend.subscription.Subscription;

public record SubscriptionResponse(
        Long id,
        Long userId,
        String serviceName,
        BigDecimal price,
        LocalDate nextPaymentDate,
        String billingCycle
) {
    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getUser().getId(),
                subscription.getServiceName(),
                subscription.getPrice(),
                subscription.getNextPaymentDate(),
                subscription.getBillingCycle()
        );
    }
}
