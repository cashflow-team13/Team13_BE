package com.cashflow.backend.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "rejected_subscriptions",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"account_id", "merchant_name"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RejectedSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "merchant_name", nullable = false)
    private String merchantName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal rejectedAmount;

    @Column(nullable = false)
    private LocalDate lastPaymentDate;

    public RejectedSubscription(
            Long accountId,
            String merchantName,
            BigDecimal rejectedAmount,
            LocalDate lastPaymentDate
    ) {
        this.accountId = accountId;
        this.merchantName = merchantName;
        this.rejectedAmount = rejectedAmount;
        this.lastPaymentDate = lastPaymentDate;
    }

    public void update(
            BigDecimal rejectedAmount,
            LocalDate lastPaymentDate
    ) {
        this.rejectedAmount = rejectedAmount;
        this.lastPaymentDate = lastPaymentDate;
    }
}
