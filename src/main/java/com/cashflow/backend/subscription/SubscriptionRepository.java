
package com.cashflow.backend.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // 특정 사용자의 구독 서비스 조회
    List<Subscription> findByUserId(Long userId);
}
