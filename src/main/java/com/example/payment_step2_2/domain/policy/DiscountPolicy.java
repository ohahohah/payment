package com.example.payment_step2_2.domain.policy;

import com.example.payment_step2_2.domain.model.Money;

/**
 * DiscountPolicy - 할인 정책 인터페이스
 *
 * [payment_step1과 동일 - Money 사용]
 */
public interface DiscountPolicy {
    /**
     * 할인 금액 계산
     * @param price 원래 가격 (Money)
     * @return 할인 금액 (Money)
     */
    Money calculateDiscount(Money price);
}
