package com.example.payment_step2.policy.tax;

import com.example.payment_step2.domain.model.Money;
import org.springframework.stereotype.Component;

/**
 * KoreaVatPolicy - 한국 부가가치세 정책 구현체
 *
 * ============================================================================
 * [payment_ul에서 변경된 점]
 * ============================================================================
 *
 * 변경 전:
 *   public double apply(double discountedPrice) {
 *       return discountedPrice * 1.1;  // 직접 연산
 *   }
 *
 * 변경 후:
 *   public Money apply(Money discountedPrice) {
 *       return discountedPrice.multiply(1.1);  // Money 메서드 사용
 *   }
 *
 * [변경 효과]
 * - Money.multiply()가 반올림 처리
 * - 연산 로직이 Value Object 안에 캡슐화
 */
@Component
public class KoreaVatPolicy implements TaxPolicy {

    private static final double VAT_RATE = 1.1;  // 10% VAT

    @Override
    public Money apply(Money discountedPrice) {
        return discountedPrice.multiply(VAT_RATE);
    }
}
