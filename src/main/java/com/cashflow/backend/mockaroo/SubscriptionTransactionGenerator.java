
package com.cashflow.backend.mockaroo;

import com.cashflow.backend.account.Account;
import com.cashflow.backend.transaction.Transaction;
import com.cashflow.backend.transaction.TransactionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionTransactionGenerator {

    private final TransactionRepository transactionRepository;

    @Transactional
    public int generate(Account account, int months) {

        if (months < 1 || months > 12) {
            throw new IllegalArgumentException(
                    "생성 기간은 1~12개월이어야 합니다."
            );
        }

        List<Transaction> transactions = new ArrayList<>();

        LocalDate currentMonth = LocalDate.now()
                .withDayOfMonth(1);

        for (int i = months - 1; i >= 0; i--) {

            LocalDate month = currentMonth.minusMonths(i);

            // 넷플릭스: 매월 10일
            addTransaction(
                    transactions,
                    account,
                    new BigDecimal("13500"),
                    "넷플릭스",
                    month.withDayOfMonth(10)
            );

            // 유튜브 프리미엄: 매월 15일
            addTransaction(
                    transactions,
                    account,
                    new BigDecimal("14900"),
                    "유튜브 프리미엄",
                    month.withDayOfMonth(15)
            );
        }

        transactionRepository.saveAll(transactions);

        return transactions.size();
    }

    private void addTransaction(
            List<Transaction> transactions,
            Account account,
            BigDecimal amount,
            String merchantName,
            LocalDate date
    ) {

        // 미래 날짜의 거래는 생성하지 않음
        if (date.isAfter(LocalDate.now())) {
            return;
        }

        Transaction transaction = new Transaction(
                account,
                amount,
                "EXPENSE",
                "구독",
                merchantName,
                date
        );

        transactions.add(transaction);
    }
}
