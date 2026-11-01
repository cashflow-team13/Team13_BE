
package com.cashflow.backend.cardbenefit.controller;

import com.cashflow.backend.cardbenefit.dto.CardBenefitRequestDto;
import com.cashflow.backend.cardbenefit.dto.CardBenefitResponseDto;
import com.cashflow.backend.cardbenefit.service.CardBenefitService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/card-benefits")
@RequiredArgsConstructor
public class CardBenefitController {

    private final CardBenefitService cardBenefitService;

    // 1. 카드 혜택 등록
    @PostMapping
    public ResponseEntity<CardBenefitResponseDto> createCardBenefit(
            @Valid @RequestBody CardBenefitRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardBenefitService.createCardBenefit(request));
    }

    // 2. 카드 혜택 한 개 조회
    @GetMapping("/{id}")
    public ResponseEntity<CardBenefitResponseDto> getCardBenefit(
            @PathVariable Long id) {

        return ResponseEntity.ok(cardBenefitService.getCardBenefit(id));
    }

    // 3. 특정 카드의 혜택 전체 조회
    @GetMapping("/payment-method/{paymentMethodId}")
    public ResponseEntity<List<CardBenefitResponseDto>> getCardBenefitsByPaymentMethod(
            @PathVariable Long paymentMethodId) {

        return ResponseEntity.ok(
                cardBenefitService.getCardBenefitsByPaymentMethod(paymentMethodId));
    }

    // 4. 카드 혜택 수정
    @PutMapping("/{id}")
    public ResponseEntity<CardBenefitResponseDto> updateCardBenefit(
            @PathVariable Long id,
            @Valid @RequestBody CardBenefitRequestDto request) {

        return ResponseEntity.ok(
                cardBenefitService.updateCardBenefit(id, request));
    }

    // 5. 카드 혜택 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCardBenefit(
            @PathVariable Long id) {

        cardBenefitService.deleteCardBenefit(id);

        return ResponseEntity.noContent().build();
    }
}
