package com.example.payment_ddd_v1.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Money Value Object 단위 테스트
 *
 * [테스트 대상]
 * - Value Object 생성 및 검증
 * - 불변성 (Immutability)
 * - 동등성 (Value Equality)
 * - 연산 메서드
 */
@DisplayName("Money Value Object 테스트")
class MoneyTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("정상 금액으로 생성")
        void createWithValidAmount() {
            // when
            Money money = Money.of(10000);

            // then
            assertThat(money.getAmount()).isEqualTo(10000);
        }

        @Test
        @DisplayName("0원으로 생성 가능")
        void createWithZero() {
            // when
            Money money = Money.of(0);

            // then
            assertThat(money.getAmount()).isEqualTo(0);
        }

        @Test
        @DisplayName("zero() 팩토리 메서드")
        void createZero() {
            // when
            Money money = Money.zero();

            // then
            assertThat(money.getAmount()).isEqualTo(0);
        }

        @Test
        @DisplayName("음수 금액은 예외 발생")
        void createWithNegativeAmount() {
            // when & then
            assertThatThrownBy(() -> Money.of(-1000))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0 이상");
        }
    }

    @Nested
    @DisplayName("연산 테스트")
    class OperationTest {

        @Test
        @DisplayName("덧셈")
        void add() {
            // given
            Money money1 = Money.of(10000);
            Money money2 = Money.of(5000);

            // when
            Money result = money1.add(money2);

            // then
            assertThat(result.getAmount()).isEqualTo(15000);
            // 원본 불변 확인
            assertThat(money1.getAmount()).isEqualTo(10000);
        }

        @Test
        @DisplayName("뺄셈")
        void subtract() {
            // given
            Money money1 = Money.of(10000);
            Money money2 = Money.of(3000);

            // when
            Money result = money1.subtract(money2);

            // then
            assertThat(result.getAmount()).isEqualTo(7000);
        }

        @Test
        @DisplayName("곱셈 (세율 적용)")
        void multiply() {
            // given
            Money money = Money.of(10000);

            // when
            Money result = money.multiply(1.1);  // 10% 세금

            // then
            assertThat(result.getAmount()).isEqualTo(11000);
        }

        @Test
        @DisplayName("크기 비교")
        void isGreaterThan() {
            // given
            Money money1 = Money.of(10000);
            Money money2 = Money.of(5000);

            // then
            assertThat(money1.isGreaterThan(money2)).isTrue();
            assertThat(money2.isGreaterThan(money1)).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동등")
        void equalsWithSameValue() {
            // given
            Money money1 = Money.of(10000);
            Money money2 = Money.of(10000);

            // then
            assertThat(money1).isEqualTo(money2);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 다름")
        void notEqualsWithDifferentValue() {
            // given
            Money money1 = Money.of(10000);
            Money money2 = Money.of(20000);

            // then
            assertThat(money1).isNotEqualTo(money2);
        }
    }
}
