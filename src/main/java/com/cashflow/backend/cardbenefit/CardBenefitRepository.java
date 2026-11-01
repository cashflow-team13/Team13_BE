
package com.cashflow.backend.cardbenefit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardBenefitRepository
        extends JpaRepository<CardBenefit, Long> {

    List<CardBenefit> findByPaymentMethodId(Long paymentMethodId);

}
