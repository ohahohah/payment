package com.example.payment.unit.policy;

import com.example.payment.policy.discount.DefaultDiscountPolicy;
import com.example.payment.policy.discount.DiscountPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ============================================================================
 * [GOOD] DiscountPolicyTest - 할인 정책 단위 테스트
 * ============================================================================
 *
 * [좋은 테스트의 특징]
 *
 * 1. 스프링 컨텍스트 불필요 (POJO 테스트)
 *    - @SpringBootTest 없음 → 밀리초 단위로 빠르게 실행
 *    - 순수 JUnit + AssertJ로만 테스트
 *
 * 2. 테스트 구조화
 *    - @Nested로 관련 테스트 그룹화
 *    - @DisplayName으로 명확한 테스트 설명
 *    - Given-When-Then 패턴 사용
 *
 * 3. 파라미터화 테스트
 *    - @ParameterizedTest로 여러 케이스를 간결하게 테스트
 *    - @CsvSource, @ValueSource 활용
 *
 * 4. 상수 사용
 *    - 매직 넘버 대신 의미 있는 상수명 사용
 *    - 테스트 의도가 명확해짐
 *
 * [단위 테스트 (Unit Test)란?]
 * - 가장 작은 단위(메서드, 클래스)를 격리하여 테스트
 * - 외부 의존성 없음 (DB, 네트워크, 스프링 등)
 * - 빠른 피드백 (밀리초 단위)
 * - 개발 중 수시로 실행
 */
@DisplayName("할인 정책 단위 테스트")
class DiscountPolicyTest {

    // 테스트에 사용할 상수들 - 매직 넘버 제거
    private static final double ORIGINAL_PRICE = 10000.0;
    private static final double VIP_DISCOUNT_RATE = 0.85;      // 15% 할인
    private static final double NORMAL_DISCOUNT_RATE = 0.90;   // 10% 할인

    // 테스트 대상 (System Under Test)
    private DiscountPolicy discountPolicy;

    /**
     * [@BeforeEach]
     * - 각 테스트 메서드 실행 전에 호출
     * - 테스트 간 격리를 위해 매번 새 인스턴스 생성
     * - 테스트 데이터 초기화
     */
    @BeforeEach
    void setUp() {
        // 매 테스트마다 새로운 인스턴스 생성 → 테스트 격리
        discountPolicy = new DefaultDiscountPolicy();
    }

    /**
     * [@Nested]
     * - 관련 테스트들을 논리적으로 그룹화
     * - 테스트 리포트 가독성 향상
     * - 공통 설정 공유 가능
     */
    @Nested
    @DisplayName("VIP 고객 할인 테스트")
    class VipDiscountTest {

        @Test
        @DisplayName("VIP 고객은 15% 할인이 적용된다")
        void shouldApply15PercentDiscountForVip() {
            // Given - 테스트 조건 설정
            double originalPrice = ORIGINAL_PRICE;
            boolean isVip = true;

            // When - 테스트 대상 실행
            double discountedPrice = discountPolicy.apply(originalPrice, isVip);

            // Then - 결과 검증
            double expectedPrice = originalPrice * VIP_DISCOUNT_RATE;  // 8500
            assertThat(discountedPrice)
                    .as("VIP 고객은 15% 할인이 적용되어야 합니다")  // 실패 시 메시지
                    .isEqualTo(expectedPrice);
        }

        /**
         * [@ParameterizedTest]
         * - 여러 입력값으로 같은 테스트 로직 반복
         * - 테스트 코드 중복 제거
         *
         * [@CsvSource]
         * - CSV 형식으로 테스트 데이터 제공
         * - "입력값, 기대값" 형태
         */
        @ParameterizedTest(name = "원가 {0}원 → VIP 할인가 {1}원")
        @DisplayName("다양한 금액에 대해 VIP 할인이 정확히 적용된다")
        @CsvSource({
                "10000, 8500",    // 10,000원 → 8,500원
                "20000, 17000",   // 20,000원 → 17,000원
                "50000, 42500",   // 50,000원 → 42,500원
                "100000, 85000",  // 100,000원 → 85,000원
                "0, 0"            // 0원 → 0원 (경계값)
        })
        void shouldCalculateCorrectVipDiscount(double originalPrice, double expectedPrice) {
            // When
            double discountedPrice = discountPolicy.apply(originalPrice, true);

            // Then
            assertThat(discountedPrice).isEqualTo(expectedPrice);
        }
    }

    @Nested
    @DisplayName("일반 고객 할인 테스트")
    class NormalDiscountTest {

        @Test
        @DisplayName("일반 고객은 10% 할인이 적용된다")
        void shouldApply10PercentDiscountForNormal() {
            // Given
            double originalPrice = ORIGINAL_PRICE;
            boolean isVip = false;

            // When
            double discountedPrice = discountPolicy.apply(originalPrice, isVip);

            // Then
            double expectedPrice = originalPrice * NORMAL_DISCOUNT_RATE;  // 9000
            assertThat(discountedPrice)
                    .as("일반 고객은 10% 할인이 적용되어야 합니다")
                    .isEqualTo(expectedPrice);
        }

        @ParameterizedTest(name = "원가 {0}원 → 일반 할인가 {1}원")
        @DisplayName("다양한 금액에 대해 일반 할인이 정확히 적용된다")
        @CsvSource({
                "10000, 9000",
                "20000, 18000",
                "50000, 45000",
                "100000, 90000",
                "0, 0"
        })
        void shouldCalculateCorrectNormalDiscount(double originalPrice, double expectedPrice) {
            // When
            double discountedPrice = discountPolicy.apply(originalPrice, false);

            // Then
            assertThat(discountedPrice).isEqualTo(expectedPrice);
        }
    }

    @Nested
    @DisplayName("할인율 비교 테스트")
    class DiscountComparisonTest {

        @Test
        @DisplayName("VIP가 일반보다 더 많은 할인을 받는다")
        void vipShouldGetMoreDiscountThanNormal() {
            // Given
            double originalPrice = ORIGINAL_PRICE;

            // When
            double vipPrice = discountPolicy.apply(originalPrice, true);
            double normalPrice = discountPolicy.apply(originalPrice, false);

            // Then
            assertThat(vipPrice)
                    .as("VIP 가격이 일반 가격보다 낮아야 합니다")
                    .isLessThan(normalPrice);

            // 할인 금액 차이 검증
            double vipDiscount = originalPrice - vipPrice;      // 1500
            double normalDiscount = originalPrice - normalPrice; // 1000
            assertThat(vipDiscount)
                    .as("VIP의 할인 금액이 일반보다 커야 합니다")
                    .isGreaterThan(normalDiscount);
        }

        /**
         * [@ValueSource]
         * - 단일 타입의 여러 값으로 테스트
         * - doubles, ints, strings 등 지원
         */
        @ParameterizedTest(name = "원가 {0}원일 때 VIP가 더 저렴")
        @DisplayName("모든 금액에서 VIP가 일반보다 저렴하다")
        @ValueSource(doubles = {1000, 5000, 10000, 50000, 100000})
        void vipAlwaysCheaperThanNormal(double originalPrice) {
            // When
            double vipPrice = discountPolicy.apply(originalPrice, true);
            double normalPrice = discountPolicy.apply(originalPrice, false);

            // Then
            assertThat(vipPrice).isLessThan(normalPrice);
        }
    }
}
