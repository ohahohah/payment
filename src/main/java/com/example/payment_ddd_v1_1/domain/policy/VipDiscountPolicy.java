package com.example.payment_ddd_v1_1.domain.policy;

import com.example.payment_ddd_v1_1.domain.model.Money;
import org.springframework.stereotype.Component;

/**
 * VipDiscountPolicy - VIP 고객 할인 (10%)
 */
@Component
public class VipDiscountPolicy implements DiscountPolicy {

    private static final double DISCOUNT_RATE = 0.10;

    @Override
    public Money calculateDiscount(Money price) {
        return price.multiply(DISCOUNT_RATE);
    }
}
