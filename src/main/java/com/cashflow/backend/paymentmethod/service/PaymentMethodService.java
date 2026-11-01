
package com.cashflow.backend.paymentmethod.service;

import com.cashflow.backend.paymentmethod.PaymentMethod;
import com.cashflow.backend.paymentmethod.PaymentMethodRepository;
import com.cashflow.backend.paymentmethod.dto.PaymentMethodRequestDto;
import com.cashflow.backend.paymentmethod.dto.PaymentMethodResponseDto;
import com.cashflow.backend.user.User;
import com.cashflow.backend.user.UserRepository;
import com.cashflow.backend.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;

    // 1. 결제수단 등록
    @Transactional
    public PaymentMethodResponseDto createPaymentMethod(
            PaymentMethodRequestDto request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        PaymentMethod paymentMethod = new PaymentMethod(
                user,
                request.getCardName(),
                request.getCardType()
        );

        PaymentMethod saved = paymentMethodRepository.save(paymentMethod);

        return toResponseDto(saved);
    }

    // 2. 결제수단 한 개 조회
    @Transactional(readOnly = true)
    public PaymentMethodResponseDto getPaymentMethod(Long id) {

        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("결제수단을 찾을 수 없습니다."));

        return toResponseDto(paymentMethod);
    }

    // 3. 특정 사용자의 결제수단 전체 조회
    @Transactional(readOnly = true)
    public List<PaymentMethodResponseDto> getPaymentMethodsByUser(Long userId) {

        return paymentMethodRepository.findByUserId(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    // 4. 결제수단 수정
    @Transactional
    public PaymentMethodResponseDto updatePaymentMethod(
            Long id, PaymentMethodRequestDto request) {

        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("결제수단을 찾을 수 없습니다."));

        paymentMethod.update(
                request.getCardName(),
                request.getCardType()
        );

        return toResponseDto(paymentMethod);
    }

    // 5. 결제수단 삭제
    @Transactional
    public void deletePaymentMethod(Long id) {

        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("결제수단을 찾을 수 없습니다."));

        paymentMethodRepository.delete(paymentMethod);
    }

    // Entity를 ResponseDto로 변환
    private PaymentMethodResponseDto toResponseDto(
            PaymentMethod paymentMethod) {

        return new PaymentMethodResponseDto(
                paymentMethod.getId(),
                paymentMethod.getUser().getId(),
                paymentMethod.getCardName(),
                paymentMethod.getCardType()
        );
    }
}
