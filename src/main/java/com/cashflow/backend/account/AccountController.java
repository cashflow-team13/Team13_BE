
package com.cashflow.backend.account;

import com.cashflow.backend.account.dto.AccountCreateRequest;
import com.cashflow.backend.account.dto.AccountResponse;
import com.cashflow.backend.account.dto.AccountUpdateRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // 1. 계좌 생성
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountCreateRequest request
    ) {
        AccountResponse response = accountService.createAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 2. 계좌 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }

    // 3. 전체 계좌 조회
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    // 4. 계좌 이름 수정
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequest request
    ) {
        return ResponseEntity.ok(
                accountService.updateAccount(id, request)
        );
    }

    // 5. 계좌 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable Long id
    ) {
        accountService.deleteAccount(id);

        return ResponseEntity.noContent().build();
    }
}
