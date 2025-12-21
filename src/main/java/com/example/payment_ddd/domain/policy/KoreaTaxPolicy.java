package com.example.payment_ddd.domain.policy;

import com.example.payment_ddd.domain.model.Country;
import com.example.payment_ddd.domain.model.Money;

/**
 * KoreaTaxPolicy - 한국 세금 정책 구현체
 *
 * [전략 패턴 구현]
 * - TaxPolicy 인터페이스를 구현
 * - 한국 VAT 10% 적용
 *
 * [도메인 지식 캡슐화]
 * - "한국은 VAT 10%"라는 비즈니스 규칙이 명시
 * - 세율 변경 시 이 클래스만 수정
 */
public class KoreaTaxPolicy implements TaxPolicy {

    private static final double KOREA_VAT_RATE = 0.10;

    @Override
    public boolean supports(Country country) {
        return country.isKorea();
    }

    @Override
    public Money applyTax(Money amount) {
        double taxMultiplier = 1 + KOREA_VAT_RATE;
        return amount.multiply(taxMultiplier);
    }
}
