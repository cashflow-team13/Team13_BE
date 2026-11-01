
package com.cashflow.backend.paymentmethod;

import com.cashflow.backend.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_methods")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String cardName;

    @Column(nullable = false)
    private String cardType;

    public PaymentMethod(User user, String cardName, String cardType) {
        this.user = user;
        this.cardName = cardName;
        this.cardType = cardType;
    }

    public void update(String cardName, String cardType) {
        this.cardName = cardName;
        this.cardType = cardType;
    }
}
