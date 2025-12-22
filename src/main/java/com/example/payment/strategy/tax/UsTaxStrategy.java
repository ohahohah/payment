package com.example.payment.strategy.tax;

import org.springframework.stereotype.Component;

/**
 * UsTaxStrategy - 미국 세금 전략 (7% Sales Tax)
 */
@Component
public class UsTaxStrategy implements TaxStrategy {

    private static final double TAX_RATE = 0.07;

    @Override
    public double apply(double discountedPrice) {
        return Math.round(discountedPrice * (1 + TAX_RATE));
    }
}
