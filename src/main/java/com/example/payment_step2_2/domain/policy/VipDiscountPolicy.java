package com.example.payment_step2_2.domain.policy;

import com.example.payment_step2_2.domain.model.Money;
import org.springframework.stereotype.Component;

/**
 * VipDiscountPolicy - VIP 고객 할인 정책 (10%)
 *
 * [payment_step1과 동일 - Money 사용]
 */
@Component
public class VipDiscountPolicy implements DiscountPolicy {

    private static final double DISCOUNT_RATE = 0.10;

    @Override
    public Money calculateDiscount(Money price) {
        return price.multiply(DISCOUNT_RATE);
    }
}
