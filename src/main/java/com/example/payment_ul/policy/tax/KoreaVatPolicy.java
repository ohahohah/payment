package com.example.payment_ul.policy.tax;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * KoreaVatPolicy - 한국 부가가치세 정책 (10% VAT)
 */
@Component
@Primary
public class KoreaVatPolicy implements TaxPolicy {

    private static final double VAT_RATE = 0.1;

    @Override
    public double apply(double discountedPrice) {
        return Math.round(discountedPrice * (1 + VAT_RATE));
    }
}
