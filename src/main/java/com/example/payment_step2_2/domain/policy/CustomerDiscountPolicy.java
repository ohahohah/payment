package com.example.payment_step2_2.domain.policy;

import com.example.payment_step2_2.domain.model.Money;
import org.springframework.stereotype.Component;

/**
 * CustomerDiscountPolicy - 일반 고객 할인 정책 (5%)
 *
 * [payment_step1과 동일 - Money 사용]
 */
@Component
public class CustomerDiscountPolicy implements DiscountPolicy {

    private static final double DISCOUNT_RATE = 0.05;

    @Override
    public Money calculateDiscount(Money price) {
        return price.multiply(DISCOUNT_RATE);
    }
}
