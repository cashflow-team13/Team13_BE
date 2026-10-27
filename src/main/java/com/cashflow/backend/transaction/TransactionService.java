
package com.cashflow.backend.transaction;

import com.cashflow.backend.account.Account;
import com.cashflow.backend.account.AccountRepository;
import com.cashflow.backend.transaction.dto.TransactionCreateRequest;
import com.cashflow.backend.transaction.dto.TransactionResponse;
import com.cashflow.backend.transaction.dto.TransactionUpdateRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    // 1. 거래 등록
    @Transactional
    public TransactionResponse createTransaction(
            TransactionCreateRequest request
    ) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() ->
                        new IllegalArgumentException("계좌를 찾을 수 없습니다."));

        validateTransaction(request.amount(), request.type());

        // 계좌 잔액 반영
        applyBalance(account, request.amount(), request.type());

        Transaction transaction = new Transaction(
                account,
                request.amount(),
                request.type(),
                request.category(),
                request.merchantName(),
                request.transactionDate()
        );

        Transaction saved = transactionRepository.save(transaction);

        return TransactionResponse.from(saved);
    }

    // 2. 거래 단건 조회
    public TransactionResponse getTransaction(Long transactionId) {
        Transaction transaction = findTransaction(transactionId);
        return TransactionResponse.from(transaction);
    }

    // 3. 전체 거래 조회
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    // 4. 특정 계좌 거래 조회
    public List<TransactionResponse> getTransactionsByAccount(Long accountId) {
        return transactionRepository.findByAccountId(accountId)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    // 5. 거래 수정
    @Transactional
    public TransactionResponse updateTransaction(
            Long transactionId,
            TransactionUpdateRequest request
    ) {
        Transaction transaction = findTransaction(transactionId);
        Account account = transaction.getAccount();

        validateTransaction(request.amount(), request.type());

        // 기존 거래의 잔액 반영 취소
        reverseBalance(
                account,
                transaction.getAmount(),
                transaction.getType()
        );

        // 수정된 거래의 잔액 반영
        applyBalance(account, request.amount(), request.type());

        transaction.updateTransaction(
                request.amount(),
                request.type(),
                request.category(),
                request.merchantName(),
                request.transactionDate()
        );

        return TransactionResponse.from(transaction);
    }

    // 6. 거래 삭제
    @Transactional
    public void deleteTransaction(Long transactionId) {
        Transaction transaction = findTransaction(transactionId);
        Account account = transaction.getAccount();

        // 삭제할 거래의 잔액 반영 취소
        reverseBalance(
                account,
                transaction.getAmount(),
                transaction.getType()
        );

        transactionRepository.delete(transaction);
    }

    // 거래 조회 공통 메서드
    private Transaction findTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new IllegalArgumentException("거래내역을 찾을 수 없습니다."));
    }

    // 거래 금액과 유형 검증
    private void validateTransaction(BigDecimal amount, String type) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("거래 금액은 0보다 커야 합니다.");
        }

        if (!"INCOME".equals(type) && !"EXPENSE".equals(type)) {
            throw new IllegalArgumentException("올바르지 않은 거래 유형입니다.");
        }
    }

    // 거래 금액을 잔액에 반영
    private void applyBalance(
            Account account, BigDecimal amount, String type
    ) {
        if ("INCOME".equals(type)) {
            account.increaseBalance(amount);
        } else if ("EXPENSE".equals(type)) {
            account.decreaseBalance(amount);
        }
    }

    // 기존 거래의 잔액 반영 취소
    private void reverseBalance(
            Account account, BigDecimal amount, String type
    ) {
        if ("INCOME".equals(type)) {
            account.decreaseBalance(amount);
        } else if ("EXPENSE".equals(type)) {
            account.increaseBalance(amount);
        }
    }
}
