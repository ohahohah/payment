package com.example.payment_ddd_v1_1.domain.policy;

import com.example.payment_ddd_v1_1.domain.model.Money;

/**
 * DiscountPolicy - 할인 정책 인터페이스 (순수 Java)
 */
public interface DiscountPolicy {
    Money calculateDiscount(Money price);
}
