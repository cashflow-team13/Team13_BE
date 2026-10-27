
package com.cashflow.backend.mockaroo;

import com.cashflow.backend.account.Account;
import com.cashflow.backend.account.AccountRepository;
import com.cashflow.backend.transaction.Transaction;
import com.cashflow.backend.transaction.TransactionRepository;
import com.cashflow.backend.transaction.dto.MockTransactionResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MockarooImportService {

    private final MockarooClient mockarooClient;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SubscriptionTransactionGenerator
        subscriptionTransactionGenerator;

    @Transactional
    public int importTransactions(Long accountId, int count) {

        // 1. 계좌 조회
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "계좌를 찾을 수 없습니다."
                        )
                );

        // 2. Mockaroo API 호출
        List<MockTransactionResponse> mockTransactions =
                mockarooClient.getTransactions(count);

        // 3. 거래내역 변환
        List<Transaction> transactions = mockTransactions.stream()
                .map(mock -> {

                    if (mock.amount() == null ||
                            mock.amount().compareTo(BigDecimal.ZERO) <= 0 ||
                            mock.transactionDate() == null) {
                        throw new IllegalArgumentException(
                                "잘못된 거래 데이터가 포함되어 있습니다."
                        );
                    }

                    String merchantName = mock.merchantName();

                    String category = classifyCategory(
                            merchantName,
                            mock.category()
                    );

                    return new Transaction(
                            account,
                            mock.amount(),
                            "EXPENSE",
                            category,
                            merchantName,
                            mock.transactionDate()
                    );
                })
                .toList();

        // 4. MySQL 저장
        transactionRepository.saveAll(transactions);

        return transactions.size();

        
    }

    @Transactional
    public int generateSubscriptions(Long accountId, int months) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "계좌를 찾을 수 없습니다."
                        )
                );

        return subscriptionTransactionGenerator.generate(
                account,
                months
        );
    }

    // 거래처 기반 카테고리 자동 분류
    private String classifyCategory(
            String merchantName,
            String originalCategory
    ) {

        if (merchantName == null) {
            return originalCategory;
        }

        return switch (merchantName.trim()) {
            case "스타벅스" -> "카페";
            case "맥도날드" -> "식비";
            case "쿠팡" -> "쇼핑";
            case "넷플릭스", "유튜브 프리미엄" -> "구독";
            case "편의점" -> "생활";
            default -> originalCategory;
        };
    }
}
