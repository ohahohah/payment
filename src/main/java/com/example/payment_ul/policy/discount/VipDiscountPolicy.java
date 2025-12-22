package com.example.payment_ul.policy.discount;

import org.springframework.stereotype.Component;

/**
 * VipDiscountPolicy - VIP/일반 고객 할인 정책
 *
 * - VIP 고객: 15% 할인
 * - 일반 고객: 10% 할인
 */
@Component
public class VipDiscountPolicy implements CustomerDiscountPolicy {

    private static final double VIP_DISCOUNT_RATE = 0.85;
    private static final double NORMAL_DISCOUNT_RATE = 0.90;

    @Override
    public double apply(double originalPrice, boolean isVip) {
        if (isVip) {
            return Math.round(originalPrice * VIP_DISCOUNT_RATE);
        }
        return Math.round(originalPrice * NORMAL_DISCOUNT_RATE);
    }
}
