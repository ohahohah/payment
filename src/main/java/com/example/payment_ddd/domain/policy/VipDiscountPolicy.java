package com.example.payment_ddd.domain.policy;

import com.example.payment_ddd.domain.model.Money;

/**
 * VipDiscountPolicy - VIP 할인 정책 구현체
 *
 * [단일 책임 원칙(SRP)]
 * - VIP 할인이라는 하나의 책임만 가짐
 * - 할인율 변경 시 이 클래스만 수정
 *
 * [도메인 지식 캡슐화]
 * - "VIP는 10% 할인"이라는 비즈니스 규칙이 코드에 명시
 */
public class VipDiscountPolicy implements DiscountPolicy {

    private static final double VIP_DISCOUNT_RATE = 0.10;

    @Override
    public Money applyDiscount(Money originalPrice, boolean isVip) {
        if (isVip) {
            double discountMultiplier = 1 - VIP_DISCOUNT_RATE;
            return originalPrice.multiply(discountMultiplier);
        }
        return originalPrice;
    }
}
