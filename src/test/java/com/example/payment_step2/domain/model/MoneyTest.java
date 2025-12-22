package com.example.payment_step2.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * MoneyTest - Money Value Object 단위 테스트
 *
 * [Value Object 테스트 포인트]
 * 1. 불변성 - 연산 후 새 객체 반환, 원본 불변
 * 2. 동등성 - 같은 값이면 equals() true
 * 3. 자가 검증 - 유효하지 않은 값 거부
 *
 * [테스트 특징]
 * - Spring 없이 순수 Java로 테스트
 * - DB 불필요 → 빠른 실행
 * - 도메인 로직만 검증
 */
@DisplayName("Money Value Object 테스트")
class MoneyTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("양수 금액으로 생성 성공")
        void createWithPositiveAmount() {
            // Given & When
            Money money = Money.of(10000);

            // Then
            assertThat(money.getAmount()).isEqualTo(10000);
        }

        @Test
        @DisplayName("0원으로 생성 가능")
        void createWithZero() {
            // Given & When
            Money money = Money.zero();

            // Then
            assertThat(money.getAmount()).isZero();
        }

        @Test
        @DisplayName("음수 금액은 거부 - 자가 검증")
        void rejectNegativeAmount() {
            // When & Then
            assertThatThrownBy(() -> Money.of(-1000))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0 이상");
        }
    }

    @Nested
    @DisplayName("연산 테스트")
    class OperationTest {

        @Test
        @DisplayName("금액 더하기 - 새 객체 반환")
        void add() {
            // Given
            Money money1 = Money.of(10000);
            Money money2 = Money.of(5000);

            // When
            Money result = money1.add(money2);

            // Then
            assertThat(result.getAmount()).isEqualTo(15000);
            // 불변성 확인 - 원본 변경 없음!
            assertThat(money1.getAmount()).isEqualTo(10000);
            assertThat(money2.getAmount()).isEqualTo(5000);
        }

        @Test
        @DisplayName("비율 곱하기 - VIP 할인 (10%)")
        void multiplyForDiscount() {
            // Given
            Money originalPrice = Money.of(10000);

            // When - 10% 할인 = 0.9 곱하기
            Money discountedPrice = originalPrice.multiply(0.9);

            // Then
            assertThat(discountedPrice.getAmount()).isEqualTo(9000);
            // 불변성 확인
            assertThat(originalPrice.getAmount()).isEqualTo(10000);
        }

        @Test
        @DisplayName("비율 곱하기 - 세금 추가 (10%)")
        void multiplyForTax() {
            // Given
            Money price = Money.of(10000);

            // When - VAT 10% = 1.1 곱하기
            Money taxedPrice = price.multiply(1.1);

            // Then
            assertThat(taxedPrice.getAmount()).isEqualTo(11000);
        }

        @Test
        @DisplayName("비율 곱하기 - 반올림 처리")
        void multiplyWithRounding() {
            // Given
            Money price = Money.of(10001);

            // When - 10001 * 0.9 = 9000.9 → 반올림 → 9001
            Money discounted = price.multiply(0.9);

            // Then
            assertThat(discounted.getAmount()).isEqualTo(9001);
        }
    }

    @Nested
    @DisplayName("비교 테스트")
    class ComparisonTest {

        @Test
        @DisplayName("금액 비교 - 큰 경우 true")
        void isGreaterThan_true() {
            // Given
            Money bigger = Money.of(10000);
            Money smaller = Money.of(5000);

            // Then
            assertThat(bigger.isGreaterThan(smaller)).isTrue();
        }

        @Test
        @DisplayName("금액 비교 - 작은 경우 false")
        void isGreaterThan_false() {
            // Given
            Money smaller = Money.of(5000);
            Money bigger = Money.of(10000);

            // Then
            assertThat(smaller.isGreaterThan(bigger)).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 테스트 - Value Object의 핵심")
    class EqualityTest {

        @Test
        @DisplayName("같은 금액은 동등 (equals)")
        void equalsByValue() {
            // Given
            Money money1 = Money.of(10000);
            Money money2 = Money.of(10000);

            // Then - 다른 인스턴스지만 값이 같으면 같다!
            assertThat(money1).isEqualTo(money2);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }

        @Test
        @DisplayName("다른 금액은 동등하지 않음")
        void notEqualsByDifferentValue() {
            // Given
            Money money1 = Money.of(10000);
            Money money2 = Money.of(20000);

            // Then
            assertThat(money1).isNotEqualTo(money2);
        }
    }
}
