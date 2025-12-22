package com.example.payment_ddd_v1.domain.policy;

import com.example.payment_ddd_v1.domain.model.Money;
import org.springframework.stereotype.Component;

/**
 * VipDiscountPolicy - VIP 할인 정책 구현체
 *
 * - VIP: 15% 할인
 * - 일반: 10% 할인
 */
@Component
public class VipDiscountPolicy implements DiscountPolicy {

    private static final double VIP_DISCOUNT_RATE = 0.85;
    private static final double NORMAL_DISCOUNT_RATE = 0.90;

    @Override
    public Money apply(Money price, boolean isVip) {
        double rate = isVip ? VIP_DISCOUNT_RATE : NORMAL_DISCOUNT_RATE;
        return price.multiply(rate);
    }
}
