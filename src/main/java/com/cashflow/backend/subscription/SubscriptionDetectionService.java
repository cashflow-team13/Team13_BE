
package com.cashflow.backend.subscription;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cashflow.backend.subscription.dto.SubscriptionDetectionResponse;
import com.cashflow.backend.transaction.Transaction;
import com.cashflow.backend.transaction.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionDetectionService {

    private final TransactionRepository transactionRepository;

    public List<SubscriptionDetectionResponse> detect(Long accountId) {

        // 1. 해당 계좌의 지출 거래만 조회
        List<Transaction> expenses = transactionRepository
                .findByAccountId(accountId)
                .stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .filter(t -> t.getMerchantName() != null)
                .filter(t -> !t.getMerchantName().isBlank())
                .filter(t -> t.getTransactionDate() != null)
                .filter(t -> t.getAmount() != null)
                .filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        // 2. 거래처별 그룹화
        Map<String, List<Transaction>> grouped = expenses.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getMerchantName().trim()
                ));

        List<SubscriptionDetectionResponse> results = new ArrayList<>();

        // 3. 거래처별 반복결제 분석
        for (Map.Entry<String, List<Transaction>> entry : grouped.entrySet()) {

            String merchantName = entry.getKey();

            List<Transaction> transactions = entry.getValue()
                    .stream()
                    .sorted(Comparator.comparing(Transaction::getTransactionDate))
                    .toList();

            // 최소 3회 이상 결제
            if (transactions.size() < 3) {
                continue;
            }

            // 4. 결제 간격 계산
            List<Long> intervals = new ArrayList<>();

            for (int i = 1; i < transactions.size(); i++) {
                long days = ChronoUnit.DAYS.between(
                        transactions.get(i - 1).getTransactionDate(),
                        transactions.get(i).getTransactionDate()
                );
                intervals.add(days);
            }

            double averageInterval = intervals.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0);

            // 월간 결제: 평균 26~35일
            if (averageInterval < 26 || averageInterval > 35) {
                continue;
            }

            // 개별 결제 간격도 월간 범위에 들어야 함
            boolean regularInterval = intervals.stream()
                    .allMatch(days -> days >= 26 && days <= 35);

            if (!regularInterval) {
                continue;
            }

            // 5. 결제금액 유사도 분석
            BigDecimal total = transactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal averageAmount = total.divide(
                    BigDecimal.valueOf(transactions.size()),
                    0,
                    RoundingMode.HALF_UP
            );

            // 평균 금액 대비 ±10% 허용
            boolean similarAmounts = transactions.stream()
                    .allMatch(t -> {
                        BigDecimal difference = t.getAmount()
                                .subtract(averageAmount)
                                .abs();

                        return difference.compareTo(
                                averageAmount.multiply(
                                        new BigDecimal("0.10")
                                )
                        ) <= 0;
                    });

            if (!similarAmounts) {
                continue;
            }

            // 6. 다음 결제일 예측
            LocalDate lastPaymentDate = transactions
                    .get(transactions.size() - 1)
                    .getTransactionDate();

            long roundedInterval = Math.round(averageInterval);

            LocalDate nextExpectedPaymentDate =
                    lastPaymentDate.plusDays(roundedInterval);

            // 7. 탐지 결과 저장
            results.add(new SubscriptionDetectionResponse(
                    merchantName,
                    averageAmount,
                    transactions.size(),
                    roundedInterval,
                    lastPaymentDate,
                    nextExpectedPaymentDate
            ));
        }

        results.sort(
                Comparator.comparing(SubscriptionDetectionResponse::merchantName)
        );

        return results;
    }
}
