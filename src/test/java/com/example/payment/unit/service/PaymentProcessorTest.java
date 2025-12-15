package com.example.payment.unit.service;

import com.example.payment.dto.PaymentResult;
import com.example.payment.listener.PaymentListener;
import com.example.payment.policy.discount.DiscountPolicy;
import com.example.payment.policy.tax.TaxPolicy;
import com.example.payment.service.PaymentProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * ============================================================================
 * [GOOD] PaymentProcessorTest - 결제 처리기 단위 테스트 (Mock 사용)
 * ============================================================================
 *
 * [테스트 특징]
 * - Mockito를 사용한 의존성 모킹
 * - 스프링 컨텍스트 없이 순수 단위 테스트
 * - BDD 스타일 (given-when-then)
 *
 * [@ExtendWith(MockitoExtension.class)]
 * - JUnit 5에서 Mockito를 사용하기 위한 확장
 * - @Mock 어노테이션이 자동으로 처리됨
 * - 스프링 없이 Mock 객체 생성 가능
 *
 * [Mock vs Stub vs Fake]
 * - Mock: 행위(호출 여부) 검증에 사용
 * - Stub: 특정 입력에 대한 출력을 정의
 * - Fake: 실제 구현의 단순화 버전
 *
 * Mockito는 Mock + Stub 기능을 모두 제공합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentProcessor 단위 테스트")
class PaymentProcessorTest {

    /**
     * [@Mock]
     * - 가짜(Mock) 객체를 생성합니다
     * - 실제 구현 없이 인터페이스나 클래스의 동작을 흉내냅니다
     * - given()으로 행동을 정의하고, then()으로 검증합니다
     */
    @Mock
    private DiscountPolicy discountPolicy;

    @Mock
    private TaxPolicy taxPolicy;

    @Mock
    private PaymentListener listener1;

    @Mock
    private PaymentListener listener2;

    // 테스트 대상 (System Under Test)
    private PaymentProcessor paymentProcessor;

    // 테스트 데이터
    private static final double ORIGINAL_PRICE = 10000.0;
    private static final double DISCOUNTED_PRICE = 8500.0;
    private static final double TAXED_PRICE = 9350.0;
    private static final String COUNTRY = "KR";

    @BeforeEach
    void setUp() {
        // Mock 리스너 리스트 생성
        List<PaymentListener> listeners = Arrays.asList(listener1, listener2);

        // 테스트 대상 생성 - Mock 객체 주입
        paymentProcessor = new PaymentProcessor(discountPolicy, taxPolicy, listeners);
    }

    @Nested
    @DisplayName("정상 결제 처리 테스트")
    class SuccessfulPaymentTest {

        @Test
        @DisplayName("결제 처리 시 할인 정책과 세금 정책이 순서대로 적용된다")
        void shouldApplyDiscountThenTax() {
            // Given - Mock 동작 정의 (BDD 스타일)
            given(discountPolicy.apply(anyDouble(), anyBoolean()))
                    .willReturn(DISCOUNTED_PRICE);
            given(taxPolicy.apply(anyDouble()))
                    .willReturn(TAXED_PRICE);

            // When
            PaymentResult result = paymentProcessor.process(ORIGINAL_PRICE, COUNTRY, true);

            // Then - 결과 검증
            assertThat(result.originalPrice()).isEqualTo(ORIGINAL_PRICE);
            assertThat(result.discountedAmount()).isEqualTo(DISCOUNTED_PRICE);
            assertThat(result.taxedAmount()).isEqualTo(TAXED_PRICE);
            assertThat(result.country()).isEqualTo(COUNTRY);
            assertThat(result.isVip()).isTrue();
        }

        @Test
        @DisplayName("할인 정책에 올바른 파라미터가 전달된다")
        void shouldPassCorrectParametersToDiscountPolicy() {
            // Given
            given(discountPolicy.apply(anyDouble(), anyBoolean()))
                    .willReturn(DISCOUNTED_PRICE);
            given(taxPolicy.apply(anyDouble()))
                    .willReturn(TAXED_PRICE);

            // When
            paymentProcessor.process(ORIGINAL_PRICE, COUNTRY, true);

            // Then - 호출 검증 (BDD 스타일)
            then(discountPolicy)
                    .should(times(1))  // 1번 호출되어야 함
                    .apply(ORIGINAL_PRICE, true);
        }

        @Test
        @DisplayName("세금 정책에 할인된 가격이 전달된다")
        void shouldPassDiscountedPriceToTaxPolicy() {
            // Given
            given(discountPolicy.apply(anyDouble(), anyBoolean()))
                    .willReturn(DISCOUNTED_PRICE);
            given(taxPolicy.apply(anyDouble()))
                    .willReturn(TAXED_PRICE);

            // When
            paymentProcessor.process(ORIGINAL_PRICE, COUNTRY, true);

            // Then - 할인된 가격이 세금 정책에 전달되었는지 검증
            then(taxPolicy)
                    .should(times(1))
                    .apply(DISCOUNTED_PRICE);  // 할인된 가격이 전달되어야 함
        }

        @Test
        @DisplayName("결제 완료 시 모든 리스너에게 알림이 전송된다")
        void shouldNotifyAllListeners() {
            // Given
            given(discountPolicy.apply(anyDouble(), anyBoolean()))
                    .willReturn(DISCOUNTED_PRICE);
            given(taxPolicy.apply(anyDouble()))
                    .willReturn(TAXED_PRICE);

            // When
            paymentProcessor.process(ORIGINAL_PRICE, COUNTRY, true);

            // Then - 모든 리스너가 호출되었는지 검증
            then(listener1)
                    .should(times(1))
                    .onPaymentCompleted(any(PaymentResult.class));

            then(listener2)
                    .should(times(1))
                    .onPaymentCompleted(any(PaymentResult.class));
        }
    }

    @Nested
    @DisplayName("VIP/일반 고객 처리 테스트")
    class CustomerTypeTest {

        @Test
        @DisplayName("VIP 고객 결제 시 isVip=true가 전달된다")
        void shouldPassVipFlagTrue() {
            // Given
            given(discountPolicy.apply(anyDouble(), anyBoolean()))
                    .willReturn(DISCOUNTED_PRICE);
            given(taxPolicy.apply(anyDouble()))
                    .willReturn(TAXED_PRICE);

            // When
            PaymentResult result = paymentProcessor.process(ORIGINAL_PRICE, COUNTRY, true);

            // Then
            assertThat(result.isVip()).isTrue();
            then(discountPolicy).should().apply(ORIGINAL_PRICE, true);
        }

        @Test
        @DisplayName("일반 고객 결제 시 isVip=false가 전달된다")
        void shouldPassVipFlagFalse() {
            // Given
            given(discountPolicy.apply(anyDouble(), anyBoolean()))
                    .willReturn(9000.0);  // 일반 고객 할인
            given(taxPolicy.apply(anyDouble()))
                    .willReturn(9900.0);

            // When
            PaymentResult result = paymentProcessor.process(ORIGINAL_PRICE, COUNTRY, false);

            // Then
            assertThat(result.isVip()).isFalse();
            then(discountPolicy).should().apply(ORIGINAL_PRICE, false);
        }
    }

    @Nested
    @DisplayName("예외 처리 테스트")
    class ExceptionTest {

        @Test
        @DisplayName("음수 가격 입력 시 예외가 발생한다")
        void shouldThrowExceptionForNegativePrice() {
            // Given
            double negativePrice = -1000.0;

            // When & Then
            assertThatThrownBy(() ->
                    paymentProcessor.process(negativePrice, COUNTRY, true))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("잘못된 가격");
        }

        @Test
        @DisplayName("음수 가격 입력 시 할인 정책이 호출되지 않는다")
        void shouldNotCallDiscountPolicyForNegativePrice() {
            // Given
            double negativePrice = -1000.0;

            // When
            try {
                paymentProcessor.process(negativePrice, COUNTRY, true);
            } catch (IllegalArgumentException ignored) {
            }

            // Then - 예외 발생으로 할인 정책이 호출되지 않아야 함
            then(discountPolicy)
                    .should(never())
                    .apply(anyDouble(), anyBoolean());
        }

        @Test
        @DisplayName("음수 가격 입력 시 리스너가 호출되지 않는다")
        void shouldNotNotifyListenersForNegativePrice() {
            // Given
            double negativePrice = -1000.0;

            // When
            try {
                paymentProcessor.process(negativePrice, COUNTRY, true);
            } catch (IllegalArgumentException ignored) {
            }

            // Then - 리스너가 호출되지 않아야 함
            then(listener1).should(never()).onPaymentCompleted(any());
            then(listener2).should(never()).onPaymentCompleted(any());
        }
    }

    @Nested
    @DisplayName("리스너 없는 경우 테스트")
    class NoListenersTest {

        @Test
        @DisplayName("리스너가 없어도 결제 처리는 정상 동작한다")
        void shouldWorkWithoutListeners() {
            // Given - 리스너 없는 PaymentProcessor
            PaymentProcessor processorWithoutListeners = new PaymentProcessor(
                    discountPolicy, taxPolicy, Collections.emptyList()
            );

            given(discountPolicy.apply(anyDouble(), anyBoolean()))
                    .willReturn(DISCOUNTED_PRICE);
            given(taxPolicy.apply(anyDouble()))
                    .willReturn(TAXED_PRICE);

            // When
            PaymentResult result = processorWithoutListeners.process(
                    ORIGINAL_PRICE, COUNTRY, true
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.taxedAmount()).isEqualTo(TAXED_PRICE);
        }
    }
}
