package com.example.payment_ddd_v1.domain.policy;

import com.example.payment_ddd_v1.domain.model.Money;

/**
 * DiscountPolicy - 할인 정책 인터페이스
 *
 * [Policy 패턴]
 * - 비즈니스 규칙을 별도 객체로 캡슐화
 * - 전략 패턴의 DDD 버전 (도메인 용어 사용)
 */
public interface DiscountPolicy {

    Money apply(Money price, boolean isVip);
}
