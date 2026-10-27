
package com.cashflow.backend.account;

import com.cashflow.backend.account.dto.AccountCreateRequest;
import com.cashflow.backend.account.dto.AccountResponse;
import com.cashflow.backend.account.dto.AccountUpdateRequest;
import com.cashflow.backend.user.User;
import com.cashflow.backend.user.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    // 1. 계좌 생성
    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Account account = new Account(
                request.accountName(),
                request.balance(),
                user
        );

        Account savedAccount = accountRepository.save(account);

        return AccountResponse.from(savedAccount);
    }

    // 2. 계좌 단건 조회
    public AccountResponse getAccount(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException("계좌를 찾을 수 없습니다."));

        return AccountResponse.from(account);
    }

    // 3. 전체 계좌 조회
    public List<AccountResponse> getAllAccounts() {

        return accountRepository.findAll()
                .stream()
                .map(AccountResponse::from)
                .toList();
    }

    // 4. 계좌 이름 수정
    @Transactional
    public AccountResponse updateAccount(
            Long accountId,
            AccountUpdateRequest request
    ) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException("계좌를 찾을 수 없습니다."));

        account.updateAccountName(request.accountName());

        return AccountResponse.from(account);
    }

    // 5. 계좌 삭제
    @Transactional
    public void deleteAccount(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException("계좌를 찾을 수 없습니다."));

        accountRepository.delete(account);
    }
}
