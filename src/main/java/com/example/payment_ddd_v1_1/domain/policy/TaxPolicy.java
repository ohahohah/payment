package com.example.payment_ddd_v1_1.domain.policy;

import com.example.payment_ddd_v1_1.domain.model.Money;

/**
 * TaxPolicy - 세금 정책 인터페이스 (순수 Java)
 */
public interface TaxPolicy {
    Money applyTax(Money price);
}
