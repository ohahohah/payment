package com.example.payment_step2.policy.tax;

import com.example.payment_step2.domain.model.Money;

/**
 * TaxPolicy - 세금 정책 인터페이스
 *
 * ============================================================================
 * [payment_ul에서 변경된 점]
 * ============================================================================
 *
 * 변경 전: double apply(double discountedPrice)
 * 변경 후: Money apply(Money discountedPrice)
 *
 * [변경 이유]
 * - Money 타입으로 금액임을 명확히 표현
 * - Money.multiply()로 세금 계산
 */
public interface TaxPolicy {

    /**
     * 세금을 적용하여 최종 금액을 계산합니다
     *
     * @param discountedPrice 할인 적용 후 가격 (Money Value Object)
     * @return 세금 적용 후 최종 가격 (Money Value Object)
     */
    Money apply(Money discountedPrice);
}
