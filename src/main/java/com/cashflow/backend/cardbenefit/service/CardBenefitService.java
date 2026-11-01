
package com.cashflow.backend.cardbenefit.service;
import com.cashflow.backend.exception.ResourceNotFoundException;

import com.cashflow.backend.cardbenefit.CardBenefit;
import com.cashflow.backend.cardbenefit.CardBenefitRepository;
import com.cashflow.backend.cardbenefit.dto.CardBenefitRequestDto;
import com.cashflow.backend.cardbenefit.dto.CardBenefitResponseDto;
import com.cashflow.backend.paymentmethod.PaymentMethod;
import com.cashflow.backend.paymentmethod.PaymentMethodRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardBenefitService {

    private final CardBenefitRepository cardBenefitRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    // 1. 카드 혜택 등록
    @Transactional
    public CardBenefitResponseDto createCardBenefit(
            CardBenefitRequestDto request) {

        PaymentMethod paymentMethod = paymentMethodRepository
                .findById(request.getPaymentMethodId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("결제수단을 찾을 수 없습니다."));

        CardBenefit cardBenefit = new CardBenefit(
                paymentMethod,
                request.getCategory(),
                request.getBenefitType(),
                request.getBenefitValue()
        );

        CardBenefit saved = cardBenefitRepository.save(cardBenefit);

        return toResponseDto(saved);
    }

    // 2. 카드 혜택 한 개 조회
    @Transactional(readOnly = true)
    public CardBenefitResponseDto getCardBenefit(Long id) {

        CardBenefit cardBenefit = cardBenefitRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("카드 혜택을 찾을 수 없습니다."));

        return toResponseDto(cardBenefit);
    }

    // 3. 특정 카드의 혜택 전체 조회
    @Transactional(readOnly = true)
    public List<CardBenefitResponseDto> getCardBenefitsByPaymentMethod(
            Long paymentMethodId) {

        return cardBenefitRepository.findByPaymentMethodId(paymentMethodId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    // 4. 카드 혜택 수정
    @Transactional
    public CardBenefitResponseDto updateCardBenefit(
            Long id, CardBenefitRequestDto request) {

        CardBenefit cardBenefit = cardBenefitRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("카드 혜택을 찾을 수 없습니다."));

        cardBenefit.update(
                request.getCategory(),
                request.getBenefitType(),
                request.getBenefitValue()
        );

        return toResponseDto(cardBenefit);
    }

    // 5. 카드 혜택 삭제
    @Transactional
    public void deleteCardBenefit(Long id) {

        CardBenefit cardBenefit = cardBenefitRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("카드 혜택을 찾을 수 없습니다."));

        cardBenefitRepository.delete(cardBenefit);
    }

    // Entity를 ResponseDto로 변환
    private CardBenefitResponseDto toResponseDto(CardBenefit cardBenefit) {

        return new CardBenefitResponseDto(
                cardBenefit.getId(),
                cardBenefit.getPaymentMethod().getId(),
                cardBenefit.getCategory(),
                cardBenefit.getBenefitType(),
                cardBenefit.getBenefitValue()
        );
    }
}
