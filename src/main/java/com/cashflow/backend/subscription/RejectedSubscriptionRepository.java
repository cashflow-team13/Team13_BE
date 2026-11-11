package com.cashflow.backend.subscription;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RejectedSubscriptionRepository
        extends JpaRepository<RejectedSubscription, Long> {

    List<RejectedSubscription> findByAccountId(Long accountId);

    Optional<RejectedSubscription>
    findByAccountIdAndMerchantNameIgnoreCase(
            Long accountId,
            String merchantName
    );
}
