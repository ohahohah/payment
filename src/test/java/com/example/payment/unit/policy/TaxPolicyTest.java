package com.example.payment.unit.policy;

import com.example.payment.strategy.tax.KoreaTaxStrategy;
import com.example.payment.strategy.tax.TaxStrategy;
import com.example.payment.strategy.tax.UsTaxStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ============================================================================
 * [GOOD] TaxPolicyTest - 세금 정책 단위 테스트
 * ============================================================================
 *
 * [테스트 특징]
 * - 순수 POJO 테스트 (스프링 불필요)
 * - 국가별 정책을 별도 @Nested 클래스로 분리
 * - 각 정책의 세율이 정확히 적용되는지 검증
 */
@DisplayName("세금 정책 단위 테스트")
class TaxPolicyTest {

    // 세율 상수 정의
    private static final double KOREA_TAX_RATE = 0.10;  // 한국 부가세 10%
    private static final double US_TAX_RATE = 0.07;     // 미국 판매세 7%

    @Nested
    @DisplayName("한국 세금 정책 (KoreaTaxPolicy)")
    class KoreaTaxPolicyTest {

        private final TaxStrategy taxStrategy = new KoreaTaxStrategy();

        @Test
        @DisplayName("한국은 10% 부가가치세가 적용된다")
        void shouldApply10PercentVat() {
            // Given
            double discountedPrice = 10000.0;

            // When
            double taxedPrice = taxStrategy.apply(discountedPrice);

            // Then
            double expectedPrice = discountedPrice * (1 + KOREA_TAX_RATE);  // 11000
            assertThat(taxedPrice)
                    .as("한국은 10%% 부가가치세가 적용되어야 합니다")
                    .isEqualTo(expectedPrice);
        }

        @ParameterizedTest(name = "할인가 {0}원 → 세금 적용 후 {1}원")
        @DisplayName("다양한 금액에 대해 한국 세금이 정확히 적용된다")
        @CsvSource({
                "10000, 11000",
                "8500, 9350",     // VIP 할인 후 금액
                "9000, 9900",     // 일반 할인 후 금액
                "50000, 55000",
                "0, 0"
        })
        void shouldCalculateCorrectKoreaTax(double discountedPrice, double expectedPrice) {
            // When
            double taxedPrice = taxStrategy.apply(discountedPrice);

            // Then
            assertThat(taxedPrice).isEqualTo(expectedPrice);
        }

        @Test
        @DisplayName("세금은 항상 원래 가격보다 높아야 한다")
        void taxedPriceShouldBeHigherThanOriginal() {
            // Given
            double discountedPrice = 10000.0;

            // When
            double taxedPrice = taxStrategy.apply(discountedPrice);

            // Then
            assertThat(taxedPrice)
                    .isGreaterThan(discountedPrice)
                    .as("세금 적용 후 가격은 원래 가격보다 높아야 합니다");
        }
    }

    @Nested
    @DisplayName("미국 세금 정책 (UsTaxPolicy)")
    class UsTaxPolicyTest {

        private final TaxStrategy taxStrategy = new UsTaxStrategy();

        @Test
        @DisplayName("미국은 7% 판매세가 적용된다")
        void shouldApply7PercentSalesTax() {
            // Given
            double discountedPrice = 10000.0;

            // When
            double taxedPrice = taxStrategy.apply(discountedPrice);

            // Then
            double expectedPrice = discountedPrice * (1 + US_TAX_RATE);  // 10700
            assertThat(taxedPrice)
                    .as("미국은 7%% 판매세가 적용되어야 합니다")
                    .isEqualTo(expectedPrice);
        }

        @ParameterizedTest(name = "할인가 {0}원 → 세금 적용 후 {1}원")
        @DisplayName("다양한 금액에 대해 미국 세금이 정확히 적용된다")
        @CsvSource({
                "10000, 10700",
                "8500, 9095",     // VIP 할인 후 금액
                "9000, 9630",     // 일반 할인 후 금액
                "50000, 53500",
                "0, 0"
        })
        void shouldCalculateCorrectUsTax(double discountedPrice, double expectedPrice) {
            // When
            double taxedPrice = taxStrategy.apply(discountedPrice);

            // Then
            assertThat(taxedPrice).isEqualTo(expectedPrice);
        }
    }

    @Nested
    @DisplayName("국가별 세금 비교 테스트")
    class TaxComparisonTest {

        private final TaxStrategy koreaTaxStrategy = new KoreaTaxStrategy();
        private final TaxStrategy usTaxStrategy = new UsTaxStrategy();

        @Test
        @DisplayName("같은 금액에 대해 한국 세금이 미국보다 높다")
        void koreaTaxShouldBeHigherThanUsTax() {
            // Given
            double discountedPrice = 10000.0;

            // When
            double koreaTaxed = koreaTaxStrategy.apply(discountedPrice);
            double usTaxed = usTaxStrategy.apply(discountedPrice);

            // Then
            assertThat(koreaTaxed)
                    .as("한국 세율(10%%)이 미국 세율(7%%)보다 높으므로 세금 후 금액도 높아야 합니다")
                    .isGreaterThan(usTaxed);

            // 세금 금액 차이 검증
            double koreaTaxAmount = koreaTaxed - discountedPrice;  // 1000
            double usTaxAmount = usTaxed - discountedPrice;        // 700
            assertThat(koreaTaxAmount).isEqualTo(1000);
            assertThat(usTaxAmount).isEqualTo(700);
        }

        @Test
        @DisplayName("세율 차이는 3%포인트이다 (10% - 7%)")
        void taxRateDifferenceShouldBe3Percent() {
            // Given
            double basePrice = 100000.0;  // 계산하기 쉬운 금액

            // When
            double koreaTaxed = koreaTaxStrategy.apply(basePrice);
            double usTaxed = usTaxStrategy.apply(basePrice);

            // Then
            double difference = koreaTaxed - usTaxed;
            assertThat(difference)
                    .as("세율 차이 3%%에 해당하는 금액이어야 합니다")
                    .isEqualTo(basePrice * 0.03);  // 3000
        }
    }
}
