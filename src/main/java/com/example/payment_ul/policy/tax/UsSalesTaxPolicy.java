package com.example.payment_ul.policy.tax;

import org.springframework.stereotype.Component;

/**
 * UsSalesTaxPolicy - 미국 판매세 정책 (7% Sales Tax)
 */
@Component
public class UsSalesTaxPolicy implements TaxPolicy {

    private static final double SALES_TAX_RATE = 0.07;

    @Override
    public double apply(double discountedPrice) {
        return Math.round(discountedPrice * (1 + SALES_TAX_RATE));
    }
}
