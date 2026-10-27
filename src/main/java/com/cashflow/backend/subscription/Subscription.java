
package com.cashflow.backend.subscription;

import com.cashflow.backend.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 구독 서비스 이름
    @Column(nullable = false)
    private String serviceName;

    // 정기 결제 금액
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    // 다음 결제 예정일
    private LocalDate nextPaymentDate;

    // 결제 주기 (MONTHLY / YEARLY)
    @Column(nullable = false)
    private String billingCycle;

    // 구독 서비스를 이용하는 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Subscription(String serviceName, BigDecimal price,
                        LocalDate nextPaymentDate, String billingCycle,
                        User user) {
        this.serviceName = serviceName;
        this.price = price;
        this.nextPaymentDate = nextPaymentDate;
        this.billingCycle = billingCycle;
        this.user = user;
    }
}
