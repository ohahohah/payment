package com.example.payment_step4_1.domain.model;

import java.util.Objects;

/**
 * Money - 금액 Value Object
 */
public class Money {

    private final double amount;

    private Money(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다: " + amount);
        }
        this.amount = amount;
    }

    public static Money of(double amount) {
        return new Money(amount);
    }

    public static Money zero() {
        return new Money(0);
    }

    public Money add(Money other) {
        return new Money(this.amount + other.amount);
    }

    public Money subtract(Money other) {
        return new Money(this.amount - other.amount);
    }

    public Money multiply(double rate) {
        return new Money(Math.round(this.amount * rate));
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Double.compare(money.amount, amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return String.format("%.0f원", amount);
    }
}
