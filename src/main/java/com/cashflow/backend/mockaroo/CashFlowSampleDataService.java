
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

        
        // 3. 구독 결제 데이터 추가
        result.addAll(generateSubscriptionData(startDate, today));

        // 4. 날짜순 정렬
        return result.stream()
                .sorted((a, b) ->
                        a.transactionDate().compareTo(b.transactionDate()))
                .toList();

    }

    
    private List<MockTransactionResponse> generateSubscriptionData(
            LocalDate startDate,
            LocalDate today
    ) {
        List<MockTransactionResponse> subscriptions = new ArrayList<>();

        // 서비스 이름과 월 결제금액
        String[] services = {
                "넷플릭스",
                "유튜브 프리미엄",
                "티빙",
                "디즈니+",
                "뮤직박스"
        };

        long[] prices = {
                13500,
                14900,
                13500,
                9900,
                7900
        };

        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int serviceIndex = 0; serviceIndex < services.length; serviceIndex++) {

            // 서비스별 결제 기준일을 다르게 설정
            int baseDay = 5 + serviceIndex * 4;

            LocalDate month = startDate.withDayOfMonth(1);

            while (!month.isAfter(today)) {

                // 기준일에서 ±2일 변동
                int dayOffset = random.nextInt(-2, 3);

                LocalDate paymentDate = month
                        .withDayOfMonth(baseDay)
                        .plusDays(dayOffset);

                // 결제금액 ±500원 변동
                long amount = prices[serviceIndex]
                        + random.nextInt(-500, 501);

                if (!paymentDate.isBefore(startDate)
                        && !paymentDate.isAfter(today)) {

                    subscriptions.add(new MockTransactionResponse(
                            BigDecimal.valueOf(amount),
                            "EXPENSE",
                            "구독",
                            services[serviceIndex],
                            paymentDate
                    ));
                }

                month = month.plusMonths(1);
            }
        }

        return subscriptions;
    }

}
