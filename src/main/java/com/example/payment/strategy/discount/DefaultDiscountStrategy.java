package com.example.payment.strategy.discount;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * DefaultDiscountStrategy - 기본 할인 전략
 *
 * - VIP: 15% 할인
 * - 일반: 10% 할인
 */
@Component
@Primary
public class DefaultDiscountStrategy implements DiscountStrategy {

    @Override
    public double apply(double originalPrice, boolean isVip) {
        if (isVip) {
            return Math.round(originalPrice * 0.85);
        }
        return Math.round(originalPrice * 0.90);
    }
}
