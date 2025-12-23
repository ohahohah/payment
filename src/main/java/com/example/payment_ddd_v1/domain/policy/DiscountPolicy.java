package com.example.payment_ddd_v1.domain.policy;

import com.example.payment_ddd_v1.domain.model.Money;

/**
 * DiscountPolicy - 할인 정책 인터페이스
 *
 * [Policy 패턴]
 * - 비즈니스 규칙을 별도 객체로 캡슐화
 * - 전략 패턴의 DDD 버전 (도메인 용어 사용)
 * - VIP/일반 고객별 별도 구현체 사용
 */
public interface DiscountPolicy {

    /**
     * 할인 금액 계산
     * @param price 원래 가격 (Money)
     * @return 할인 금액 (Money)
     */
    Money calculateDiscount(Money price);
}
