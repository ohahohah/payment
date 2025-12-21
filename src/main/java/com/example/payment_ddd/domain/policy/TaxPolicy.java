package com.example.payment_ddd.domain.policy;

import com.example.payment_ddd.domain.model.Country;
import com.example.payment_ddd.domain.model.Money;

/**
 * TaxPolicy - 세금 정책 인터페이스
 *
 * [전략 패턴(Strategy Pattern)]
 * - 국가별 세금 계산 알고리즘을 캡슐화
 * - 새로운 국가 추가 시 기존 코드 수정 없이 확장 가능
 *
 * [도메인 정책(Domain Policy)]
 * - 세금 계산이라는 비즈니스 규칙을 도메인 레이어에서 관리
 * - Application Service가 아닌 Domain에서 비즈니스 로직 처리
 */
public interface TaxPolicy {

    /**
     * 해당 국가에 적용 가능한지 확인
     *
     * @param country 국가
     * @return 적용 가능 여부
     */
    boolean supports(Country country);

    /**
     * 세금 적용
     *
     * @param amount 세전 금액
     * @return 세후 금액
     */
    Money applyTax(Money amount);
}
