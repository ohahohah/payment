package com.example.payment.strategy.tax;

/**
 * TaxStrategy - 세금 전략 인터페이스
 */
public interface TaxStrategy {

    double apply(double discountedPrice);
}
