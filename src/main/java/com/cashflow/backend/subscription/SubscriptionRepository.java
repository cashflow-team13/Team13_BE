
package com.cashflow.backend.subscription;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserId(Long userId);

    boolean existsByUserIdAndServiceNameIgnoreCase(
            Long userId,
            String serviceName
    );
}
