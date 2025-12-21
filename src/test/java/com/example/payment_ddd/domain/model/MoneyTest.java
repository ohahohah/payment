package com.example.payment_ddd.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * MoneyTest - Money Value Object 단위 테스트
 *
 * [Value Object 테스트 포인트]
 * 1. 불변성: 연산 후 새 객체 반환
 * 2. 동등성: 값이 같으면 같은 객체
 * 3. 자가 검증: 유효하지 않은 값 거부
 */
@DisplayName("Money Value Object 테스트")
class MoneyTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("양수 금액으로 생성 성공")
        void createWithPositiveAmount() {
            Money money = Money.of(10000);
            assertThat(money.getAmount()).isEqualTo(10000);
        }

        @Test
        @DisplayName("0원으로 생성 가능")
        void createWithZero() {
            Money money = Money.zero();
            assertThat(money.getAmount()).isZero();
        }

        @Test
        @DisplayName("음수 금액은 거부")
        void rejectNegativeAmount() {
            assertThatThrownBy(() -> Money.of(-1000))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0 이상");
        }
    }

    @Nested
    @DisplayName("연산 테스트")
    class OperationTest {

        @Test
        @DisplayName("금액 더하기")
        void add() {
            Money money1 = Money.of(10000);
            Money money2 = Money.of(5000);

            Money result = money1.add(money2);

            assertThat(result.getAmount()).isEqualTo(15000);
            // 원본 불변 확인
            assertThat(money1.getAmount()).isEqualTo(10000);
        }

        @Test
        @DisplayName("비율 곱하기 (VIP 할인)")
        void multiply() {
            Money money = Money.of(10000);

            Money discounted = money.multiply(0.9); // 10% 할인

            assertThat(discounted.getAmount()).isEqualTo(9000);
        }

        @Test
        @DisplayName("비율 곱하기 (세금 추가)")
        void multiplyForTax() {
            Money money = Money.of(10000);

            Money taxed = money.multiply(1.1); // 10% 세금

            assertThat(taxed.getAmount()).isEqualTo(11000);
        }
    }

    @Nested
    @DisplayName("비교 테스트")
    class ComparisonTest {

        @Test
        @DisplayName("금액 비교 - 더 큰 경우")
        void isGreaterThan() {
            Money bigger = Money.of(10000);
            Money smaller = Money.of(5000);

            assertThat(bigger.isGreaterThan(smaller)).isTrue();
            assertThat(smaller.isGreaterThan(bigger)).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 금액은 동등")
        void equalsByValue() {
            Money money1 = Money.of(10000);
            Money money2 = Money.of(10000);

            assertThat(money1).isEqualTo(money2);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }

        @Test
        @DisplayName("다른 금액은 동등하지 않음")
        void notEqualsByDifferentValue() {
            Money money1 = Money.of(10000);
            Money money2 = Money.of(20000);

            assertThat(money1).isNotEqualTo(money2);
        }
    }
}
