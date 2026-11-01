
package com.cashflow.backend.paymentmethod.controller;

import com.cashflow.backend.paymentmethod.dto.PaymentMethodRequestDto;
import com.cashflow.backend.paymentmethod.dto.PaymentMethodResponseDto;
import com.cashflow.backend.paymentmethod.service.PaymentMethodService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    // 1. 결제수단 등록
    @PostMapping
    public ResponseEntity<PaymentMethodResponseDto> createPaymentMethod(
            @Valid @RequestBody PaymentMethodRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentMethodService.createPaymentMethod(request));
    }

    // 2. 결제수단 한 개 조회
    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodResponseDto> getPaymentMethod(
            @PathVariable Long id) {

        return ResponseEntity.ok(paymentMethodService.getPaymentMethod(id));
    }

    // 3. 특정 사용자의 결제수단 전체 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentMethodResponseDto>> getPaymentMethodsByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                paymentMethodService.getPaymentMethodsByUser(userId));
    }

    // 4. 결제수단 수정
    @PutMapping("/{id}")
    public ResponseEntity<PaymentMethodResponseDto> updatePaymentMethod(
            @PathVariable Long id,
            @Valid @RequestBody PaymentMethodRequestDto request) {

        return ResponseEntity.ok(
                paymentMethodService.updatePaymentMethod(id, request));
    }

    // 5. 결제수단 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentMethod(
            @PathVariable Long id) {

        paymentMethodService.deletePaymentMethod(id);

        return ResponseEntity.noContent().build();
    }
}
