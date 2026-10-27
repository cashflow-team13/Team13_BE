
package com.cashflow.backend.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 특정 계좌의 모든 거래내역 조회
    List<Transaction> findByAccountId(Long accountId);
}
