package com.example.payment_step2.policy.discount;

import com.example.payment_step2.domain.model.Money;

/**
 * CustomerDiscountPolicy - 고객 할인 정책 인터페이스
 *
 * ============================================================================
 * [payment_ul에서 변경된 점]
 * ============================================================================
 *
 * 변경 전: double apply(double originalPrice, boolean isVip)
 * 변경 후: Money apply(Money originalPrice, boolean isVip)
 *
 * [변경 이유]
 * 1. 타입 안전성: double 대신 Money를 사용하여 의미 명확화
 * 2. 로직 캡슐화: Money.multiply()로 할인 계산
 * 3. 자동 검증: Money.of()에서 유효성 검사
 */
public interface CustomerDiscountPolicy {

    /**
     * 고객 유형에 따른 할인을 적용합니다
     *
     * @param originalPrice 원래 가격 (Money Value Object)
     * @param isVip VIP 고객 여부
     * @return 할인 적용 후 금액 (Money Value Object)
     */
    Money apply(Money originalPrice, boolean isVip);
}
