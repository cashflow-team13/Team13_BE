
package com.cashflow.backend.mockaroo;

import com.cashflow.backend.transaction.dto.MockTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CashFlowSampleDataService {

    private final MockarooClient mockarooClient;

    public List<MockTransactionResponse> generateSampleData(
            int months,
            int count
    ) {
        if (months < 1 || months > 6) {
            throw new IllegalArgumentException(
                    "생성 기간은 1~6개월이어야 합니다."
            );
        }

        if (count < 1 || count > 1000) {
            throw new IllegalArgumentException(
                    "거래 개수는 1~1000 사이여야 합니다."
            );
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusMonths(months);

        List<MockTransactionResponse> result = new ArrayList<>();

        // 1. Mockaroo 일반 소비 데이터
        List<MockTransactionResponse> mockData =
                mockarooClient.getTransactions(count);

        for (MockTransactionResponse transaction : mockData) {
            if (transaction.transactionDate() == null
                    || transaction.amount() == null
                    || transaction.amount().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // 요청한 기간의 거래만 사용
            if (transaction.transactionDate().isBefore(startDate)
                    || transaction.transactionDate().isAfter(today)) {
                continue;
            }

            // 일반 소비 거래만 남김
            result.add(new MockTransactionResponse(
                    transaction.amount(),
                    "EXPENSE",
                    transaction.category(),
                    transaction.merchantName(),
                    transaction.transactionDate()
            ));
        }

        // 2. 월급 데이터 생성
        for (int i = months - 1; i >= 0; i--) {
            LocalDate month = today.withDayOfMonth(1).minusMonths(i);

            // 매월 25일 기준 ±2일
            int dayOffset = ThreadLocalRandom.current().nextInt(-2, 3);
            LocalDate salaryDate = month.withDayOfMonth(25)
                    .plusDays(dayOffset);

            // 월급 250만원 기준 ±5만원
            int amountOffset = ThreadLocalRandom.current()
                    .nextInt(-50000, 50001);

            if (!salaryDate.isBefore(startDate)
                    && !salaryDate.isAfter(today)) {
                result.add(new MockTransactionResponse(
                        BigDecimal.valueOf(2500000L + amountOffset),
                        "INCOME",
                        "급여",
                        "ABC회사",
                        salaryDate
                ));
            }
        }

        return result.stream()
                .sorted((a, b) ->
                        a.transactionDate().compareTo(b.transactionDate()))
                .toList();
    }
}
