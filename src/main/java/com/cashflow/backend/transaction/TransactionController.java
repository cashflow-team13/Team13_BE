
package com.cashflow.backend.transaction;

import com.cashflow.backend.transaction.dto.TransactionCreateRequest;
import com.cashflow.backend.transaction.dto.TransactionResponse;
import com.cashflow.backend.transaction.dto.TransactionUpdateRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // 1. 거래 등록
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionCreateRequest request
    ) {
        TransactionResponse response =
                transactionService.createTransaction(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 2. 거래 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                transactionService.getTransaction(id)
        );
    }

    // 3. 전체 거래 조회
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(
            @RequestParam(required = false) Long accountId
    ) {
        if (accountId != null) {
            return ResponseEntity.ok(
                    transactionService.getTransactionsByAccount(accountId)
            );
        }

        return ResponseEntity.ok(
                transactionService.getAllTransactions()
        );
    }

    // 4. 거래 수정
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request
    ) {
        return ResponseEntity.ok(
                transactionService.updateTransaction(id, request)
        );
    }

    // 5. 거래 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id
    ) {
        transactionService.deleteTransaction(id);

        return ResponseEntity.noContent().build();
    }
}
