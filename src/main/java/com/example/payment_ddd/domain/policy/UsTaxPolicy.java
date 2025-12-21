package com.example.payment_ddd.domain.policy;

import com.example.payment_ddd.domain.model.Country;
import com.example.payment_ddd.domain.model.Money;

/**
 * UsTaxPolicy - 미국 세금 정책 구현체
 *
 * [전략 패턴 구현]
 * - TaxPolicy 인터페이스를 구현
 * - 미국 Sales Tax 8% 적용
 *
 * [도메인 지식 캡슐화]
 * - "미국은 Sales Tax 8%"라는 비즈니스 규칙이 명시
 */
public class UsTaxPolicy implements TaxPolicy {

    private static final double US_SALES_TAX_RATE = 0.08;

    @Override
    public boolean supports(Country country) {
        return country.isUs();
    }

    @Override
    public Money applyTax(Money amount) {
        double taxMultiplier = 1 + US_SALES_TAX_RATE;
        return amount.multiply(taxMultiplier);
    }
}
