package com.example.payment_ddd.domain.model;

import java.util.Objects;

/**
 * Money - 금액 Value Object
 *
 * [Value Object 특징]
 * 1. 불변(Immutable): 한번 생성되면 값이 변하지 않음
 * 2. 동등성(Equality): 속성 값이 같으면 같은 객체로 취급
 * 3. 자가 검증(Self-Validation): 생성 시점에 유효성 검증
 * 4. 부수 효과 없음(Side-Effect Free): 연산 시 새 객체 반환
 *
 * [왜 double 대신 Money를 사용하나요?]
 * - 도메인 개념을 명확히 표현 (금액은 그냥 숫자가 아님)
 * - 음수 금액 등 잘못된 값 방지 (자가 검증)
 * - 금액 연산 로직을 한 곳에서 관리
 * - 타입 안전성 (컴파일 타임에 실수 방지)
 */
public class Money {

    private final double amount;

    private Money(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다: " + amount);
        }
        this.amount = amount;
    }

    /**
     * 정적 팩토리 메서드
     */
    public static Money of(double amount) {
        return new Money(amount);
    }

    /**
     * 0원
     */
    public static Money zero() {
        return new Money(0);
    }

    /**
     * 금액 값 반환
     */
    public double getAmount() {
        return amount;
    }

    /**
     * 두 금액 더하기 (새 객체 반환 - 불변)
     */
    public Money add(Money other) {
        return new Money(this.amount + other.amount);
    }

    /**
     * 비율 적용 (할인, 세금 등)
     */
    public Money multiply(double rate) {
        return new Money(Math.round(this.amount * rate));
    }

    /**
     * 특정 금액 이상인지 확인
     */
    public boolean isGreaterThan(Money other) {
        return this.amount > other.amount;
    }

    /**
     * 특정 금액 이상인지 확인
     */
    public boolean isGreaterThanOrEqual(double threshold) {
        return this.amount >= threshold;
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
        return String.format("Money(%.0f)", amount);
    }
}
