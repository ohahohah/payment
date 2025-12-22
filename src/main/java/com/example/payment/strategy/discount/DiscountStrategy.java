package com.example.payment.strategy.discount;

/**
 * DiscountStrategy - 할인 전략 인터페이스
 */
public interface DiscountStrategy {

    double apply(double originalPrice, boolean isVip);
}
