package com.example.payment_ddd_v1.domain.policy;

import com.example.payment_ddd_v1.domain.model.Money;

/**
 * TaxPolicy - 세금 정책 인터페이스
 */
public interface TaxPolicy {

    Money apply(Money price);
}
