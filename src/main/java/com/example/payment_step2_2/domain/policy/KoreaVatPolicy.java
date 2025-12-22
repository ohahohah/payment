package com.example.payment_step2_2.domain.policy;

import com.example.payment_step2_2.domain.model.Money;
import org.springframework.stereotype.Component;

/**
 * KoreaVatPolicy - 한국 부가세 정책 (10%)
 *
 * [payment_step1과 동일 - Money 사용]
 */
@Component
public class KoreaVatPolicy implements TaxPolicy {

    private static final double VAT_RATE = 0.10;

    @Override
    public Money applyTax(Money price) {
        Money tax = price.multiply(VAT_RATE);
        return price.add(tax);
    }
}
