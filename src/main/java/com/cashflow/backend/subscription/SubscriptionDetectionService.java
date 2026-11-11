
package com.cashflow.backend.subscription;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
    private final SubscriptionRepository subscriptionRepository;
    private final RejectedSubscriptionRepository rejectedSubscriptionRepository;

    // 월간 결제 허용 간격
    private static final int MIN_INTERVAL = 26;
    private static final int MAX_INTERVAL = 35;

    // 최소 반복 결제 횟수
    private static final int MIN_PAYMENT_COUNT = 3;

    // 결제 금액 허용 오차: 10%
    private static final BigDecimal AMOUNT_TOLERANCE =
            new BigDecimal("0.10");

    public List<SubscriptionDetectionResponse> detect(Long accountId) {

        // 1. 계좌의 유효한 지출 거래 조회
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

        // 3. 거래처별 반복 패턴 탐색
        for (Map.Entry<String, List<Transaction>> entry : grouped.entrySet()) {

            String merchantName = entry.getKey();

            List<Transaction> transactions = entry.getValue()
                    .stream()
                    .sorted(Comparator.comparing(
                            Transaction::getTransactionDate
                    ))
                    .toList();

            List<Transaction> bestPattern = findBestPattern(transactions);

            if (bestPattern.size() < MIN_PAYMENT_COUNT) {
                continue;
            }

            // 4. 탐지된 패턴만 사용해 평균 결제금액 계산
            BigDecimal total = bestPattern.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal averageAmount = total.divide(
                    BigDecimal.valueOf(bestPattern.size()),
                    0,
                    RoundingMode.HALF_UP
            );

            // 5. 탐지된 패턴의 평균 결제 간격
            long totalInterval = 0;

            for (int i = 1; i < bestPattern.size(); i++) {
                totalInterval += ChronoUnit.DAYS.between(
                        bestPattern.get(i - 1).getTransactionDate(),
                        bestPattern.get(i).getTransactionDate()
                );
            }

            long averageInterval = Math.round(
                    (double) totalInterval / (bestPattern.size() - 1)
            );

            LocalDate lastPaymentDate = bestPattern
                    .get(bestPattern.size() - 1)
                    .getTransactionDate();

            LocalDate nextExpectedPaymentDate =
                    lastPaymentDate.plusDays(averageInterval);

            results.add(new SubscriptionDetectionResponse(
                    merchantName,
                    averageAmount,
                    bestPattern.size(),
                    averageInterval,
                    lastPaymentDate,
                    nextExpectedPaymentDate
            ));
        }

        results.sort(
                Comparator.comparing(
                        SubscriptionDetectionResponse::merchantName
                )
        );

        return results;
    }

    // 같은 거래처에서 월간 반복 결제 패턴 찾기
    private List<Transaction> findBestPattern(List<Transaction> transactions) {

        List<Transaction> bestPattern = new ArrayList<>();

        // 모든 거래를 시작점으로 시도
        for (int i = 0; i < transactions.size(); i++) {

            List<Transaction> pattern = new ArrayList<>();
            pattern.add(transactions.get(i));

            Transaction last = transactions.get(i);

            for (int j = i + 1; j < transactions.size(); j++) {

                Transaction current = transactions.get(j);

                long interval = ChronoUnit.DAYS.between(
                        last.getTransactionDate(),
                        current.getTransactionDate()
                );

                // 너무 이른 거래는 건너뛰기
                if (interval < MIN_INTERVAL) {
                    continue;
                }

                // 허용 간격을 초과하면 현재 패턴 종료
                if (interval > MAX_INTERVAL) {
                    break;
                }

                // 금액이 비슷한지 확인
                if (!isSimilarAmount(
                        pattern.get(0).getAmount(),
                        current.getAmount()
                )) {
                    continue;
                }

                pattern.add(current);
                last = current;
            }

            if (pattern.size() > bestPattern.size()) {
                bestPattern = pattern;
            }
        }

        return bestPattern;
    }

    // 두 결제금액의 차이가 기준 금액의 10% 이내인지 검사
    private boolean isSimilarAmount(
            BigDecimal referenceAmount,
            BigDecimal currentAmount
    ) {
        BigDecimal difference = referenceAmount
                .subtract(currentAmount)
                .abs();

        BigDecimal tolerance = referenceAmount
                .multiply(AMOUNT_TOLERANCE);

        return difference.compareTo(tolerance) <= 0;
    }

    public List<SubscriptionDetectionResponse> detectAvailable(
            Long accountId,
            Long userId
    ) {
        // 기존 탐지 알고리즘 실행
        List<SubscriptionDetectionResponse> candidates = detect(accountId);

        // 이미 확정된 구독 목록
        Set<String> confirmedNames = subscriptionRepository
                .findByUserId(userId)
                .stream()
                .map(s -> s.getServiceName().trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        // 거절된 구독 기록
        Map<String, RejectedSubscription> rejectedMap =
                rejectedSubscriptionRepository.findByAccountId(accountId)
                        .stream()
                        .collect(Collectors.toMap(
                                r -> r.getMerchantName()
                                        .trim().toLowerCase(Locale.ROOT),
                                r -> r,
                                (first, second) -> first
                        ));

        return candidates.stream()
                // 이미 확정된 서비스 제외
                .filter(candidate ->
                        !confirmedNames.contains(
                                candidate.merchantName()
                                        .trim().toLowerCase(Locale.ROOT)
                        )
                )
                // 거절된 패턴 제외
                .filter(candidate -> {
                    RejectedSubscription rejected = rejectedMap.get(
                            candidate.merchantName()
                                    .trim().toLowerCase(Locale.ROOT)
                    );

                    if (rejected == null) {
                        return true;
                    }

                    // 거절 이후 새로운 결제가 발생했으면 재검토
                    if (candidate.lastPaymentDate()
                            .isAfter(rejected.getLastPaymentDate())) {
                        return true;
                    }

                    // 결제금액이 20% 이상 달라졌으면 재검토
                    BigDecimal difference = candidate.averageAmount()
                            .subtract(rejected.getRejectedAmount())
                            .abs();

                    BigDecimal threshold = rejected.getRejectedAmount()
                            .multiply(new BigDecimal("0.20"));

                    return difference.compareTo(threshold) > 0;
                })
                .toList();
    }

}
