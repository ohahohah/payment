package com.example.payment_ddd_v1.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Payment Entity 단위 테스트 (Rich Domain Model)
 *
 * [테스트 대상]
 * - 생성 로직
 * - 상태 전이 (complete, fail, refund)
 * - 비즈니스 규칙 검증
 */
@DisplayName("Payment Entity 테스트")
class PaymentTest {

    private Money originalPrice;
    private Money discountedAmount;
    private Money taxedAmount;
    private Country country;

    @BeforeEach
    void setUp() {
        originalPrice = Money.of(10000);
        discountedAmount = Money.of(9000);
        taxedAmount = Money.of(9900);
        country = Country.of("KR");
    }

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("일반 고객 결제 생성")
        void createPaymentForCustomer() {
            // when
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, false);

            // then
            assertThat(payment.getOriginalPrice()).isEqualTo(originalPrice);
            assertThat(payment.getDiscountedAmount()).isEqualTo(discountedAmount);
            assertThat(payment.getTaxedAmount()).isEqualTo(taxedAmount);
            assertThat(payment.getCountry()).isEqualTo(country);
            assertThat(payment.isVip()).isFalse();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("VIP 고객 결제 생성")
        void createPaymentForVip() {
            // when
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, true);

            // then
            assertThat(payment.isVip()).isTrue();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("결제 완료 테스트")
    class CompleteTest {

        @Test
        @DisplayName("PENDING 상태에서 완료 가능")
        void completeFromPending() {
            // given
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, false);

            // when
            payment.complete();

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("COMPLETED 상태에서 완료 시도하면 예외")
        void cannotCompleteFromCompleted() {
            // given
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, false);
            payment.complete();

            // when & then
            assertThatThrownBy(payment::complete)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("대기 상태");
        }

        @Test
        @DisplayName("FAILED 상태에서 완료 시도하면 예외")
        void cannotCompleteFromFailed() {
            // given
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, false);
            payment.fail();

            // when & then
            assertThatThrownBy(payment::complete)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("결제 실패 테스트")
    class FailTest {

        @Test
        @DisplayName("PENDING 상태에서 실패 처리 가능")
        void failFromPending() {
            // given
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, false);

            // when
            payment.fail();

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("COMPLETED 상태에서 실패 시도하면 예외")
        void cannotFailFromCompleted() {
            // given
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, false);
            payment.complete();

            // when & then
            assertThatThrownBy(payment::fail)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("환불 테스트")
    class RefundTest {

        @Test
        @DisplayName("COMPLETED 상태에서 환불 가능")
        void refundFromCompleted() {
            // given
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, false);
            payment.complete();

            // when
            payment.refund();

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("PENDING 상태에서 환불 시도하면 예외")
        void cannotRefundFromPending() {
            // given
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, false);

            // when & then
            assertThatThrownBy(payment::refund)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("완료된 결제만");
        }

        @Test
        @DisplayName("FAILED 상태에서 환불 시도하면 예외")
        void cannotRefundFromFailed() {
            // given
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, false);
            payment.fail();

            // when & then
            assertThatThrownBy(payment::refund)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("상태 전이 시나리오")
    class StateTransitionTest {

        @Test
        @DisplayName("정상 결제 흐름: PENDING -> COMPLETED -> REFUNDED")
        void normalFlowWithRefund() {
            // given
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, false);

            // when & then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

            payment.complete();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

            payment.refund();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("실패 흐름: PENDING -> FAILED")
        void failFlow() {
            // given
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, false);

            // when & then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

            payment.fail();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }
}
