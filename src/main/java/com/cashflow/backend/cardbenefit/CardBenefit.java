
package com.cashflow.backend.cardbenefit;

import com.cashflow.backend.paymentmethod.PaymentMethod;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "card_benefits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String benefitType;

    @Column(nullable = false)
    private BigDecimal benefitValue;

    public CardBenefit(
            PaymentMethod paymentMethod,
            String category,
            String benefitType,
            BigDecimal benefitValue) {

        this.paymentMethod = paymentMethod;
        this.category = category;
        this.benefitType = benefitType;
        this.benefitValue = benefitValue;
    }

    public void update(
            String category,
            String benefitType,
            BigDecimal benefitValue) {

        this.category = category;
        this.benefitType = benefitType;
        this.benefitValue = benefitValue;
    }
}
