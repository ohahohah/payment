package com.example.payment_ddd_v1_1.domain.policy;

import com.example.payment_ddd_v1_1.domain.model.Money;
import org.springframework.stereotype.Component;

/**
 * KoreaTaxPolicy - 한국 부가세 (10%)
 */
@Component
public class KoreaTaxPolicy implements TaxPolicy {

    private static final double TAX_RATE = 0.10;

    @Override
    public Money applyTax(Money price) {
        Money tax = price.multiply(TAX_RATE);
        return price.add(tax);
    }
}
