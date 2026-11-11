
package com.cashflow.backend.subscription;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.cashflow.backend.account.Account;
import com.cashflow.backend.account.AccountRepository;
import com.cashflow.backend.subscription.dto.SubscriptionConfirmRequest;
import com.cashflow.backend.subscription.dto.SubscriptionCreateRequest;
import com.cashflow.backend.subscription.dto.SubscriptionDetectionResponse;
import com.cashflow.backend.subscription.dto.SubscriptionRejectRequest;
import com.cashflow.backend.subscription.dto.SubscriptionResponse;
import com.cashflow.backend.subscription.dto.SubscriptionSummaryResponse;
import com.cashflow.backend.user.User;
import com.cashflow.backend.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionDetectionService subscriptionDetectionService;
    private final AccountRepository accountRepository;
    private final RejectedSubscriptionRepository rejectedSubscriptionRepository;

    @Transactional
    public Subscription create(SubscriptionCreateRequest request) {

        if (request.userId() == null) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "userId is required"
            );
        }

        if (request.serviceName() == null ||
                request.serviceName().isBlank()) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "serviceName is required"
            );
        }

        if (request.price() == null ||
                request.price().signum() <= 0) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "price must be positive"
            );
        }

        if (!"MONTHLY".equals(request.billingCycle()) &&
                !"YEARLY".equals(request.billingCycle())) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "Invalid billingCycle"
            );
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                NOT_FOUND, "User not found"
                        )
                );

        String serviceName = request.serviceName().trim();

        if (subscriptionRepository
                .existsByUserIdAndServiceNameIgnoreCase(
                        user.getId(), serviceName
                )) {
            throw new ResponseStatusException(
                    CONFLICT, "Subscription already exists"
            );
        }

        Subscription subscription = new Subscription(
                serviceName,
                request.price(),
                request.nextPaymentDate(),
                request.billingCycle(),
                user
        );

        return subscriptionRepository.save(subscription);
    }
    
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptions(Long userId) {

        if (userId == null) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "userId is required"
            );
        }

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    NOT_FOUND, "User not found"
            );
        }

        return subscriptionRepository.findByUserId(userId)
                .stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    @Transactional
    public Subscription confirmSubscription(
            SubscriptionConfirmRequest request
    ) {
        if (request == null ||
                request.userId() == null ||
                request.accountId() == null ||
                request.merchantName() == null ||
                request.merchantName().isBlank()) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "Required fields are missing"
            );
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "User not found"
                ));

        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "Account not found"
                ));

        // 계좌 소유자 확인
        if (account.getUser() == null ||
                !account.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    FORBIDDEN, "Account does not belong to user"
            );
        }

        String merchantName = request.merchantName().trim();

        // 최신 거래내역으로 구독 후보 재탐지
        SubscriptionDetectionResponse candidate =
                subscriptionDetectionService.detect(request.accountId())
                        .stream()
                        .filter(result ->
                                result.merchantName()
                                        .equalsIgnoreCase(merchantName))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(
                                NOT_FOUND, "Subscription candidate not found"
                        ));

        // 중복 등록 방지
        if (subscriptionRepository
                .existsByUserIdAndServiceNameIgnoreCase(
                        user.getId(), candidate.merchantName()
                )) {
            throw new ResponseStatusException(
                    CONFLICT, "Subscription already confirmed"
            );
        }

        Subscription subscription = new Subscription(
                candidate.merchantName(),
                candidate.averageAmount(),
                candidate.nextExpectedPaymentDate(),
                "MONTHLY",
                user
        );

        return subscriptionRepository.save(subscription);
    }

	@Transactional
	public void rejectSubscription(SubscriptionRejectRequest request) {

	if (request == null ||
			request.userId() == null ||
			request.accountId() == null ||
			request.merchantName() == null ||
			request.merchantName().isBlank()) {
			throw new ResponseStatusException(
					BAD_REQUEST, "Required fields are missing"
			);
	}

	User user = userRepository.findById(request.userId())
			.orElseThrow(() -> new ResponseStatusException(
					NOT_FOUND, "User not found"
			));

	Account account = accountRepository.findById(request.accountId())
			.orElseThrow(() -> new ResponseStatusException(
					NOT_FOUND, "Account not found"
			));

	if (account.getUser() == null ||
			!account.getUser().getId().equals(user.getId())) {
			throw new ResponseStatusException(
					FORBIDDEN, "Account does not belong to user"
			);
	}

	String merchantName = request.merchantName().trim();

	// 탐지된 후보 중 해당 거래처 찾기
	SubscriptionDetectionResponse candidate =
			subscriptionDetectionService.detect(request.accountId())
					.stream()
					.filter(result ->
							result.merchantName()
									.equalsIgnoreCase(merchantName))
					.findFirst()
					.orElseThrow(() -> new ResponseStatusException(
							NOT_FOUND, "Subscription candidate not found"
					));

	// 동일 계좌·거래처의 기존 거절 기록 확인
	RejectedSubscription rejection =
			rejectedSubscriptionRepository
					.findByAccountIdAndMerchantNameIgnoreCase(
							request.accountId(), merchantName
					)
					.orElse(null);

	if (rejection == null) {
			rejectedSubscriptionRepository.save(
					new RejectedSubscription(
							request.accountId(),
							candidate.merchantName(),
							candidate.averageAmount(),
							candidate.lastPaymentDate()
					)
			);
	} else {
			rejection.update(
					candidate.averageAmount(),
					candidate.lastPaymentDate()
			);
	}
	}

	@Transactional(readOnly = true)
	public SubscriptionSummaryResponse getSubscriptionSummary(Long userId) {

		if (userId == null) {
			throw new ResponseStatusException(
					BAD_REQUEST, "userId is required"
			);
		}

		if (!userRepository.existsById(userId)) {
			throw new ResponseStatusException(
					NOT_FOUND, "User not found"
			);
		}

		List<Subscription> subscriptions =
				subscriptionRepository.findByUserId(userId);

		BigDecimal monthlyTotal = BigDecimal.ZERO;

		for (Subscription subscription : subscriptions) {

			BigDecimal price = subscription.getPrice();

			if ("MONTHLY".equals(subscription.getBillingCycle())) {
				monthlyTotal = monthlyTotal.add(price);

			} else if ("YEARLY".equals(subscription.getBillingCycle())) {
				BigDecimal monthlyPrice = price.divide(
						BigDecimal.valueOf(12),
						2,
						RoundingMode.HALF_UP
				);

				monthlyTotal = monthlyTotal.add(monthlyPrice);
			}
		}

		return new SubscriptionSummaryResponse(
				userId,
				subscriptions.size(),
				monthlyTotal
		);
	}

	@Transactional(readOnly = true)
	public List<SubscriptionResponse> getUpcomingSubscriptions(Long userId) {

		if (userId == null) {
			throw new ResponseStatusException(
					BAD_REQUEST, "userId is required"
			);
		}

		if (!userRepository.existsById(userId)) {
			throw new ResponseStatusException(
					NOT_FOUND, "User not found"
			);
		}

		return subscriptionRepository.findByUserId(userId)
				.stream()
				.filter(subscription ->
						subscription.getNextPaymentDate() != null
				)
				.sorted(Comparator.comparing(
						Subscription::getNextPaymentDate
				))
				.map(SubscriptionResponse::from)
				.toList();
	}



}
