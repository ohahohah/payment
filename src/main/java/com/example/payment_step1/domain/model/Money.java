package com.example.payment_step1.domain.model;

import java.util.Objects;

/**
 * Money - 금액 Value Object
 *
 * ============================================================================
 * [Anti-DDD에서 DDD로: double → Money]
 * ============================================================================
 *
 * Anti-DDD (기존):
 *   private double originalPrice;
 *   private double discountedAmount;
 *
 * DDD (개선):
 *   private final Money originalPrice;
 *   private final Money discountedAmount;
 *
 * ============================================================================
 * [Value Object란?]
 * ============================================================================
 *
 * 1. 불변(Immutable)
 *    - 한번 생성되면 값을 변경할 수 없음
 *    - setter가 없음
 *    - 연산 결과는 새로운 객체로 반환
 *
 * 2. 동등성(Equality by Value)
 *    - 같은 값이면 같은 객체로 취급
 *    - equals(), hashCode() 재정의
 *
 * 3. 자가 검증(Self-Validation)
 *    - 생성 시점에 유효성 검증
 *    - 유효하지 않은 상태의 객체는 존재할 수 없음
 *
 * ============================================================================
 * [왜 double 대신 Money를 사용하나요?]
 * ============================================================================
 *
 * 문제 1: 타입 안전성 부족
 *   - double price = -100; // 음수 금액 허용됨!
 *   - Money price = Money.of(-100); // 예외 발생!
 *
 * 문제 2: 비즈니스 로직 분산
 *   - double discounted = price * 0.9; // 서비스 어딘가에 분산
 *   - Money discounted = price.multiply(0.9); // 로직이 객체 안에!
 *
 * 문제 3: 의미 없는 연산 방지
 *   - double total = price + country; // 컴파일 통과 (버그!)
 *   - Money total = price.add(country); // 컴파일 에러!
 */
public class Money {

    private final double amount;

    /**
     * private 생성자 - 팩토리 메서드 사용 강제
     */
    private Money(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다: " + amount);
        }
        this.amount = amount;
    }

    /**
     * 정적 팩토리 메서드
     *
     * [왜 생성자 대신 팩토리 메서드?]
     * - 의미 있는 이름 부여: Money.of(10000) vs new Money(10000)
     * - 캐싱 가능 (자주 쓰는 값)
     * - 생성 로직 확장 용이
     */
    public static Money of(double amount) {
        return new Money(amount);
    }

    /**
     * 0원 생성
     */
    public static Money zero() {
        return new Money(0);
    }

    /**
     * 금액 더하기
     *
     * [불변성 유지]
     * - this 객체는 변경하지 않음
     * - 새로운 Money 객체를 반환
     */
    public Money add(Money other) {
        return new Money(this.amount + other.amount);
    }

    /**
     * 비율 곱하기 (할인, 세금 계산용)
     *
     * 사용 예:
     * - VIP 10% 할인: money.multiply(0.9)
     * - VAT 10% 추가: money.multiply(1.1)
     */
    public Money multiply(double rate) {
        return new Money(Math.round(this.amount * rate));
    }

    /**
     * 금액 비교
     */
    public boolean isGreaterThan(Money other) {
        return this.amount > other.amount;
    }

    /**
     * 금액 반환
     */
    public double getAmount() {
        return amount;
    }

    // ==========================================================================
    // Value Object의 핵심: equals, hashCode
    // ==========================================================================

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
        return String.format("Money(%,.0f원)", amount);
    }
}
