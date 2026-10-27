
package com.cashflow.backend.mockaroo;

import com.cashflow.backend.transaction.dto.MockTransactionResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mockaroo")
@RequiredArgsConstructor
public class MockarooController {

    private final MockarooClient mockarooClient;
    private final MockarooImportService mockarooImportService;

    // Mockaroo 데이터 조회 (DB 저장 X)
    @GetMapping("/transactions")
    public List<MockTransactionResponse> getTransactions(
            @RequestParam(defaultValue = "200") int count
    ) {
        return mockarooClient.getTransactions(count);
    }

    // Mockaroo 데이터를 MySQL에 저장
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importTransactions(
            @RequestParam Long accountId,
            @RequestParam(defaultValue = "200") int count
    ) {

        int savedCount =
                mockarooImportService.importTransactions(accountId, count);

        return ResponseEntity.ok(
                Map.of(
                        "message", "거래내역 저장 완료",
                        "accountId", accountId,
                        "savedCount", savedCount
                )
        );
    }

    @PostMapping("/subscriptions/generate")
    public ResponseEntity<Map<String, Object>> generateSubscriptions(
            @RequestParam Long accountId,
            @RequestParam(defaultValue = "3") int months
    ) {

        int savedCount =
                mockarooImportService.generateSubscriptions(
                        accountId,
                        months
                );

        return ResponseEntity.ok(
                Map.of(
                        "message", "정기 구독 거래내역 생성 완료",
                        "accountId", accountId,
                        "months", months,
                        "savedCount", savedCount
                )
        );
    }
}
