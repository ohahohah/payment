package com.example.payment_ddd.domain.policy;

import com.example.payment_ddd.domain.model.Money;

/**
 * DiscountPolicy - 할인 정책 인터페이스
 *
 * [전략 패턴(Strategy Pattern)]
 * - 할인 알고리즘을 캡슐화
 * - 런타임에 할인 정책 교체 가능
 * - OCP(개방-폐쇄 원칙) 준수: 새로운 할인 정책 추가 시 기존 코드 수정 불필요
 *
 * [도메인 정책(Domain Policy)]
 * - 비즈니스 규칙을 표현하는 도메인 객체
 * - 상태 없이 순수하게 규칙만 정의
 */
public interface DiscountPolicy {

    /**
     * 할인 적용
     *
     * @param originalPrice 원래 가격
     * @param isVip VIP 여부
     * @return 할인된 금액
     */
    Money applyDiscount(Money originalPrice, boolean isVip);
}
