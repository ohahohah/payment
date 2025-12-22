package com.example.payment_step2_2.domain.model.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.payment_step2.domain.model.Country;
import com.example.payment_step2.domain.model.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * PaymentTest - Payment Aggregate Root 단위 테스트
 *
 * [테스트 포인트 - Rich Domain Model]
 * 1. 비즈니스 로직이 엔티티 내부에 있는지 확인
 * 2. 상태 전이 규칙 검증 (PENDING → COMPLETED → REFUNDED)
 * 3. setter 없이 상태 변경이 메서드로만 가능한지 확인
 *
 * [Anti-DDD와의 차이]
 * - Anti-DDD: payment.setStatus(COMPLETED) → 규칙 검증 없음
 * - DDD: payment.complete() → 규칙 검증
 */
@DisplayName("Payment Aggregate Root 테스트")
class PaymentTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("결제 생성 시 PENDING 상태")
        void createWithPendingStatus() {
            // When
            Payment payment = createSamplePayment();

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("결제 생성 시 Value Object 타입으로 금액 보유")
        void createWithMoneyValueObjects() {
            // Given
            Money original = Money.of(10000);
            Money discounted = Money.of(9000);
            Money taxed = Money.of(9900);
            Country country = Country.korea();

            // When
            Payment payment = Payment.create(original, discounted, taxed, country, true);

            // Then - Value Object 타입!
            assertThat(payment.getOriginalPrice()).isEqualTo(original);
            assertThat(payment.getDiscountedAmount()).isEqualTo(discounted);
            assertThat(payment.getTaxedAmount()).isEqualTo(taxed);
            assertThat(payment.getCountry()).isEqualTo(country);
            assertThat(payment.isVip()).isTrue();
        }
    }

    @Nested
    @DisplayName("결제 완료 테스트 - complete()")
    class CompleteTest {

        @Test
        @DisplayName("PENDING 상태에서 complete() 호출 → COMPLETED")
        void completeFromPending() {
            // Given
            Payment payment = createSamplePayment();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

            // When
            payment.complete();

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("이미 완료된 결제에 complete() 호출 → 예외")
        void cannotCompleteAlreadyCompleted() {
            // Given
            Payment payment = createSamplePayment();
            payment.complete();  // 첫 번째 완료

            // When & Then - 두 번째 완료 시도
            assertThatThrownBy(() -> payment.complete())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("대기 상태");
        }

        @Test
        @DisplayName("실패한 결제에 complete() 호출 → 예외")
        void cannotCompleteFailedPayment() {
            // Given
            Payment payment = createSamplePayment();
            payment.fail();

            // When & Then
            assertThatThrownBy(() -> payment.complete())
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("결제 실패 테스트 - fail()")
    class FailTest {

        @Test
        @DisplayName("PENDING 상태에서 fail() 호출 → FAILED")
        void failFromPending() {
            // Given
            Payment payment = createSamplePayment();

            // When
            payment.fail();

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("완료된 결제에 fail() 호출 → 예외")
        void cannotFailCompletedPayment() {
            // Given
            Payment payment = createSamplePayment();
            payment.complete();

            // When & Then
            assertThatThrownBy(() -> payment.fail())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("대기 상태");
        }
    }

    @Nested
    @DisplayName("환불 테스트 - refund()")
    class RefundTest {

        @Test
        @DisplayName("완료된 결제에 refund() 호출 → REFUNDED")
        void refundFromCompleted() {
            // Given
            Payment payment = createSamplePayment();
            payment.complete();

            // When
            payment.refund();

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("대기 상태 결제에 refund() 호출 → 예외")
        void cannotRefundPendingPayment() {
            // Given
            Payment payment = createSamplePayment();
            // PENDING 상태 유지

            // When & Then
            assertThatThrownBy(() -> payment.refund())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("완료된 결제");
        }

        @Test
        @DisplayName("실패한 결제에 refund() 호출 → 예외")
        void cannotRefundFailedPayment() {
            // Given
            Payment payment = createSamplePayment();
            payment.fail();

            // When & Then
            assertThatThrownBy(() -> payment.refund())
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("이미 환불된 결제에 refund() 호출 → 예외")
        void cannotRefundAlreadyRefundedPayment() {
            // Given
            Payment payment = createSamplePayment();
            payment.complete();
            payment.refund();

            // When & Then
            assertThatThrownBy(() -> payment.refund())
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("상태 전이 다이어그램 검증")
    class StateTransitionTest {

        @Test
        @DisplayName("정상 흐름: PENDING → COMPLETED → REFUNDED")
        void normalFlow() {
            Payment payment = createSamplePayment();

            // PENDING
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

            // PENDING → COMPLETED
            payment.complete();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

            // COMPLETED → REFUNDED
            payment.refund();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("실패 흐름: PENDING → FAILED")
        void failureFlow() {
            Payment payment = createSamplePayment();

            // PENDING → FAILED
            payment.fail();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }

    // ==========================================================================
    // 테스트 헬퍼 메서드
    // ==========================================================================

    private Payment createSamplePayment() {
        return Payment.create(
                Money.of(10000),    // 원래 가격
                Money.of(9000),     // 할인 후
                Money.of(9900),     // 세금 후
                Country.korea(),
                true                // VIP
        );
    }
}
