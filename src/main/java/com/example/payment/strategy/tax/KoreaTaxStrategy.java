package com.example.payment.strategy.tax;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * KoreaTaxStrategy - 한국 세금 전략 (10% VAT)
 */
@Component
@Primary
public class KoreaTaxStrategy implements TaxStrategy {

    private static final double TAX_RATE = 0.1;

    @Override
    public double apply(double discountedPrice) {
        return Math.round(discountedPrice * (1 + TAX_RATE));
    }
}
