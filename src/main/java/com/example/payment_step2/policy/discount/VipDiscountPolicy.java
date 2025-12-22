package com.example.payment_step2.policy.discount;

import com.example.payment_step2.domain.model.Money;
import org.springframework.stereotype.Component;

/**
 * VipDiscountPolicy - VIP 고객 할인 정책 구현체
 *
 * ============================================================================
 * [payment_ul에서 변경된 점]
 * ============================================================================
 *
 * 변경 전:
 *   public double apply(double originalPrice, boolean isVip) {
 *       if (isVip) {
 *           return originalPrice * 0.85;  // 직접 연산
 *       }
 *       return originalPrice;
 *   }
 *
 * 변경 후:
 *   public Money apply(Money originalPrice, boolean isVip) {
 *       if (isVip) {
 *           return originalPrice.multiply(0.85);  // Money 메서드 사용
 *       }
 *       return originalPrice;
 *   }
 *
 * [변경 효과]
 * - Money.multiply()가 반올림 처리를 담당
 * - 연산 로직이 Value Object 안에 캡슐화
 * - 불변성 보장 (새 Money 객체 반환)
 */
@Component
public class VipDiscountPolicy implements CustomerDiscountPolicy {

    private static final double VIP_DISCOUNT_RATE = 0.85;  // 15% 할인

    @Override
    public Money apply(Money originalPrice, boolean isVip) {
        if (isVip) {
            return originalPrice.multiply(VIP_DISCOUNT_RATE);
        }
        return originalPrice;
    }
}
