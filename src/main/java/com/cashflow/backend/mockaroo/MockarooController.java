
package com.cashflow.backend.mockaroo;

import com.cashflow.backend.transaction.dto.MockTransactionResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mockaroo")
@RequiredArgsConstructor
public class MockarooController {

    private final MockarooClient mockarooClient;

    @GetMapping("/transactions")
    public List<MockTransactionResponse> getTransactions(
            @RequestParam(defaultValue = "200") int count
    ) {
        return mockarooClient.getTransactions(count);
    }
}
