package com.example.payment_step2_2.domain.policy;

import com.example.payment_step2_2.domain.model.Money;

/**
 * TaxPolicy - 세금 정책 인터페이스
 *
 * [payment_step1과 동일 - Money 사용]
 */
public interface TaxPolicy {
    /**
     * 세금 포함 금액 계산
     * @param price 할인된 가격 (Money)
     * @return 세금 포함 최종 가격 (Money)
     */
    Money applyTax(Money price);
}
