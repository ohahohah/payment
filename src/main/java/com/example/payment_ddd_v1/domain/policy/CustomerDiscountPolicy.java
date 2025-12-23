package com.example.payment_ddd_v1.domain.policy;

import com.example.payment_ddd_v1.domain.model.Money;
import org.springframework.stereotype.Component;

/**
 * CustomerDiscountPolicy - 일반 고객 할인 정책 (5%)
 *
 * [Policy 패턴]
 * - 일반 고객에게 적용되는 할인 정책
 * - VIP 고객은 VipDiscountPolicy 사용
 */
@Component
public class CustomerDiscountPolicy implements DiscountPolicy {

    private static final double DISCOUNT_RATE = 0.05;

    @Override
    public Money calculateDiscount(Money price) {
        return price.multiply(DISCOUNT_RATE);
    }
}
