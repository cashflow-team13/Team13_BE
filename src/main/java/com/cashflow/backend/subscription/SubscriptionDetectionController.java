
package com.cashflow.backend.subscription;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cashflow.backend.subscription.dto.SubscriptionDetectionResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionDetectionController {

    private final SubscriptionDetectionService subscriptionDetectionService;

    @GetMapping("/detect")
    public List<SubscriptionDetectionResponse> detectSubscriptions(
            @RequestParam Long accountId,
            @RequestParam(required = false) Long userId
    ) {
        if (userId == null) {
            return subscriptionDetectionService.detect(accountId);
        }

        return subscriptionDetectionService.detectAvailable(
                accountId, userId
        );
    }
}
