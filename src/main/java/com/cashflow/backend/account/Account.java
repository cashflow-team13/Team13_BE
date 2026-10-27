
package com.cashflow.backend.account;

import com.cashflow.backend.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Account(String accountName, BigDecimal balance, User user) {
        this.accountName = accountName;
        this.balance = balance;
        this.user = user;
    }

    // 계좌 이름 변경
    public void updateAccountName(String accountName) {
        this.accountName = accountName;
    }
}
