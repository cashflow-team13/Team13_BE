
package com.cashflow.backend.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cashflow.backend.account.Account;
import com.cashflow.backend.subscription.dto.SubscriptionDetectionResponse;
import com.cashflow.backend.transaction.Transaction;
import com.cashflow.backend.transaction.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class SubscriptionDetectionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private SubscriptionDetectionService subscriptionDetectionService;

    private Account account;

    @BeforeEach
    void setUp() {
        // 서비스 로직은 account의 ID를 직접 사용하지 않으므로
        // 테스트용 Account 객체만 생성
        account = mock(Account.class);
    }

    private Transaction expense(
            String merchant,
            int amount,
            String date
    ) {
        return new Transaction(
                account,
                BigDecimal.valueOf(amount),
                "EXPENSE",
                "기타",
                merchant,
                LocalDate.parse(date)
        );
    }

    // 같은 거래처에서 월간 반복 결제 패턴이 3회 이상이면 반복 결제로 인식
    @Test
    void regularSubscriptionDetected() {

        List<Transaction> transactions = List.of(
                expense("넷플릭스", 13500, "2026-08-05"),
                expense("넷플릭스", 13500, "2026-09-05"),
                expense("넷플릭스", 13500, "2026-10-05")
        );

        when(transactionRepository.findByAccountId(2L))
                .thenReturn(transactions);

        List<SubscriptionDetectionResponse> result =
                subscriptionDetectionService.detect(2L);

        assertEquals(1, result.size());
        assertEquals("넷플릭스", result.get(0).merchantName());
        assertEquals(3, result.get(0).paymentCount());
    }

    // 다른 금액의 소비가 중간에 섞여 있어도 반복 결제로 인식
    @Test
    void unrelatedExpensesAreIgnored() {

        List<Transaction> transactions = List.of(
                expense("넷플릭스", 13122, "2026-08-05"),
                expense("넷플릭스", 94083, "2026-08-13"),
                expense("넷플릭스", 13458, "2026-09-07"),
                expense("넷플릭스", 58400, "2026-09-20"),
                expense("넷플릭스", 13601, "2026-10-05")
        );

        when(transactionRepository.findByAccountId(2L))
                .thenReturn(transactions);

        List<SubscriptionDetectionResponse> result =
                subscriptionDetectionService.detect(2L);

        assertEquals(1, result.size());
        assertEquals(3, result.get(0).paymentCount());
    }

    // 9~10일 간격으로 결제된 거래는 반복 결제로 인식하지 않음
    @Test
    void irregularPaymentsNotDetected() {

        List<Transaction> transactions = List.of(
                expense("카페", 5000, "2026-08-01"),
                expense("카페", 5000, "2026-08-10"),
                expense("카페", 5000, "2026-08-20")
        );

        when(transactionRepository.findByAccountId(2L))
                .thenReturn(transactions);

        List<SubscriptionDetectionResponse> result =
                subscriptionDetectionService.detect(2L);

        assertTrue(result.isEmpty());
    }

    // 같은 거래처에서 월간 반복 결제 패턴이 3회 이상이어야 반복 결제로 인식
    @Test
    void lessThanThreePaymentsNotDetected() {

        List<Transaction> transactions = List.of(
                expense("티빙", 13500, "2026-08-10"),
                expense("티빙", 13500, "2026-09-10")
        );

        when(transactionRepository.findByAccountId(2L))
                .thenReturn(transactions);

        List<SubscriptionDetectionResponse> result =
                subscriptionDetectionService.detect(2L);

        assertTrue(result.isEmpty());
    }

    // 같은 거래처에서 금액이 다른 결제는 반복 결제로 인식하지 않음
    @Test
    void differentAmountsNotDetected() {

        List<Transaction> transactions = List.of(
                expense("서비스A", 10000, "2026-08-05"),
                expense("서비스A", 20000, "2026-09-05"),
                expense("서비스A", 30000, "2026-10-05")
        );

        when(transactionRepository.findByAccountId(2L))
                .thenReturn(transactions);

        List<SubscriptionDetectionResponse> result =
                subscriptionDetectionService.detect(2L);

        assertTrue(result.isEmpty());

        
    }

        
    @Test
    void regularShoppingMayBeMistakenForSubscription() {

        List<Transaction> transactions = List.of(
                expense("편의점", 10000, "2026-08-05"),
                expense("편의점", 10500, "2026-09-05"),
                expense("편의점", 9800, "2026-10-05")
        );

        when(transactionRepository.findByAccountId(2L))
                .thenReturn(transactions);

        List<SubscriptionDetectionResponse> result =
                subscriptionDetectionService.detect(2L);

        // 일반 소비(예. 편의점)는 구독으로 탐지되지 않아야 한다는 기대
        assertEquals(1, result.size());
    }

}
