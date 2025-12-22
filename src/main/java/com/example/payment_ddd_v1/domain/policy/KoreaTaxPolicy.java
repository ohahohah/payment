package com.example.payment_ddd_v1.domain.policy;

import com.example.payment_ddd_v1.domain.model.Money;
import org.springframework.stereotype.Component;

/**
 * KoreaTaxPolicy - 한국 부가세 정책
 *
 * - 부가가치세 10% 적용
 */
@Component
public class KoreaTaxPolicy implements TaxPolicy {

    private static final double VAT_RATE = 1.10;

    @Override
    public Money apply(Money price) {
        return price.multiply(VAT_RATE);
    }
}
