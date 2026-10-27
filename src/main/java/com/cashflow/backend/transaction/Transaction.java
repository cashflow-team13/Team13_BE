
package com.cashflow.backend.transaction;

import com.cashflow.backend.account.Account;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 거래가 발생한 계좌
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // 거래 금액
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // 거래 유형 (INCOME / EXPENSE)
    @Column(nullable = false)
    private String type;

    // 소비 카테고리 (식비, 교통비 등)
    private String category;

    // 거래 날짜
    @Column(nullable = false)
    private LocalDate transactionDate;

    public Transaction(Account account, BigDecimal amount,
                       String type, String category,
                       LocalDate transactionDate) {
        this.account = account;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.transactionDate = transactionDate;
    }
}
