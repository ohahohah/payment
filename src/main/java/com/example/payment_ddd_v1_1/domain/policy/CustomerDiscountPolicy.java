package com.example.payment_ddd_v1_1.domain.policy;

import com.example.payment_ddd_v1_1.domain.model.Money;
import org.springframework.stereotype.Component;

/**
 * CustomerDiscountPolicy - 일반 고객 할인 (5%)
 */
@Component
public class CustomerDiscountPolicy implements DiscountPolicy {

    private static final double DISCOUNT_RATE = 0.05;

    @Override
    public Money calculateDiscount(Money price) {
        return price.multiply(DISCOUNT_RATE);
    }
}
