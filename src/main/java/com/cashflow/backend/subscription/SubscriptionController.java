
package com.cashflow.backend.subscription;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cashflow.backend.subscription.dto.SubscriptionConfirmRequest;
import com.cashflow.backend.subscription.dto.SubscriptionCreateRequest;
import com.cashflow.backend.subscription.dto.SubscriptionRejectRequest;
import com.cashflow.backend.subscription.dto.SubscriptionResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createSubscription(
            @RequestBody SubscriptionCreateRequest request
    ) {
        subscriptionService.create(request);
    }

    @GetMapping
    public List<SubscriptionResponse> getSubscriptions(
            @RequestParam Long userId
    ) {
        return subscriptionService.getSubscriptions(userId);
    }

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    public void confirmSubscription(
            @RequestBody SubscriptionConfirmRequest request
    ) {
        subscriptionService.confirmSubscription(request);
    }

    @PostMapping("/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectSubscription(
            @RequestBody SubscriptionRejectRequest request
    ) {
        subscriptionService.rejectSubscription(request);
    }


}
