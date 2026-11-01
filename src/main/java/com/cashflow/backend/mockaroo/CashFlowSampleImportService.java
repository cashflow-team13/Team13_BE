
package com.cashflow.backend.mockaroo;

import com.cashflow.backend.account.Account;
import com.cashflow.backend.account.AccountRepository;
import com.cashflow.backend.transaction.Transaction;
import com.cashflow.backend.transaction.TransactionRepository;
import com.cashflow.backend.transaction.dto.MockTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CashFlowSampleImportService {

    private final CashFlowSampleDataService sampleDataService;
    private final SampleDataImportRepository sampleDataImportRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public int importSampleData(Long accountId, int months, int count) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException("계좌를 찾을 수 없습니다."));

        // 이미 샘플데이터를 생성한 계좌라면 중복 저장 방지
        if (sampleDataImportRepository.existsByAccountId(accountId)) {
            throw new IllegalStateException(
                    "이미 샘플데이터가 생성된 계좌입니다."
            );
        }

        List<MockTransactionResponse> samples =
                sampleDataService.generateSampleData(months, count);

        List<Transaction> transactions = samples.stream()
                .map(sample -> new Transaction(
                        account,
                        sample.amount(),
                        sample.type(),
                        sample.category(),
                        sample.merchantName(),
                        sample.transactionDate()
                ))
                .toList();

        transactionRepository.saveAll(transactions);

        // 저장 완료 기록
        sampleDataImportRepository.save(
                new SampleDataImport(accountId)
        );

        return transactions.size();
    }
}
