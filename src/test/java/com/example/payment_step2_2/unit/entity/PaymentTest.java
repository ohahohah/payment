package com.example.payment_step2_2.unit.entity;

import com.example.payment_step2_2.domain.model.Country;
import com.example.payment_step2_2.domain.model.Money;
import com.example.payment_step2_2.entity.Payment;
import com.example.payment_step2_2.entity.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PaymentTest - Payment Aggregate 단위 테스트
 *
 * ============================================================================
 * [payment_step1과의 차이점]
 * ============================================================================
 *
 * payment_step1: setter 테스트 (단순 값 변경 확인)
 *   - payment.setStatus(PaymentStatus.COMPLETED)
 *   - 상태 전이 규칙은 Service에서 검증
 *
 * payment_step2_2: 비즈니스 메서드 테스트 (상태 전이 규칙 검증)
 *   - payment.complete() → 규칙 검증 + 상태 변경
 *   - 잘못된 상태 전이 시 예외 발생 테스트
 *
 * ============================================================================
 * [Aggregate 테스트의 중요성]
 * ============================================================================
 *
 * Rich Domain Model에서는 Entity가 비즈니스 규칙을 캡슐화하므로
 * Entity 단위 테스트가 핵심 비즈니스 로직을 검증함.
 *
 * Service 테스트는 단순해짐 (위임만 확인)
 */
@DisplayName("Payment Aggregate 테스트")
class PaymentTest {

    @Nested
    @DisplayName("create() 팩토리 메서드")
    class CreateTest {

        @Test
        @DisplayName("생성 시 PENDING 상태로 초기화")
        void shouldCreateWithPendingStatus() {
            // Given
            Money originalPrice = Money.of(10000.0);
            Money discountedAmount = Money.of(9000.0);
            Money taxedAmount = Money.of(9900.0);
            Country country = Country.of("KR");
            Boolean isVip = true;

            // When
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, isVip);

            // Then
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            assertEquals(originalPrice, payment.getOriginalPrice());
            assertEquals(discountedAmount, payment.getDiscountedAmount());
            assertEquals(taxedAmount, payment.getTaxedAmount());
            assertEquals(country, payment.getCountry());
            assertTrue(payment.getIsVip());
            assertNotNull(payment.getCreatedAt());
            assertNotNull(payment.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("complete() 비즈니스 메서드")
    class CompleteTest {

        @Test
        @DisplayName("PENDING → COMPLETED 성공")
        void shouldCompleteFromPending() {
            // Given
            Payment payment = createTestPayment();
            assertEquals(PaymentStatus.PENDING, payment.getStatus());

            // When
            payment.complete();

            // Then
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        }

        @Test
        @DisplayName("COMPLETED 상태에서 complete() 호출 시 예외")
        void shouldThrowExceptionWhenAlreadyCompleted() {
            // Given
            Payment payment = createTestPayment();
            payment.complete();
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());

            // When & Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> payment.complete());

            assertTrue(exception.getMessage().contains("대기 상태의 결제만 완료할 수 있습니다"));
        }

        @Test
        @DisplayName("FAILED 상태에서 complete() 호출 시 예외")
        void shouldThrowExceptionWhenFailed() {
            // Given
            Payment payment = createTestPayment();
            payment.fail();

            // When & Then
            assertThrows(IllegalStateException.class, () -> payment.complete());
        }

        @Test
        @DisplayName("REFUNDED 상태에서 complete() 호출 시 예외")
        void shouldThrowExceptionWhenRefunded() {
            // Given
            Payment payment = createTestPayment();
            payment.complete();
            payment.refund();

            // When & Then
            assertThrows(IllegalStateException.class, () -> payment.complete());
        }
    }

    @Nested
    @DisplayName("fail() 비즈니스 메서드")
    class FailTest {

        @Test
        @DisplayName("PENDING → FAILED 성공")
        void shouldFailFromPending() {
            // Given
            Payment payment = createTestPayment();

            // When
            payment.fail();

            // Then
            assertEquals(PaymentStatus.FAILED, payment.getStatus());
        }

        @Test
        @DisplayName("COMPLETED 상태에서 fail() 호출 시 예외")
        void shouldThrowExceptionWhenCompleted() {
            // Given
            Payment payment = createTestPayment();
            payment.complete();

            // When & Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> payment.fail());

            assertTrue(exception.getMessage().contains("대기 상태의 결제만 실패 처리할 수 있습니다"));
        }
    }

    @Nested
    @DisplayName("refund() 비즈니스 메서드")
    class RefundTest {

        @Test
        @DisplayName("COMPLETED → REFUNDED 성공")
        void shouldRefundFromCompleted() {
            // Given
            Payment payment = createTestPayment();
            payment.complete();
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());

            // When
            payment.refund();

            // Then
            assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        }

        @Test
        @DisplayName("PENDING 상태에서 refund() 호출 시 예외")
        void shouldThrowExceptionWhenPending() {
            // Given
            Payment payment = createTestPayment();
            assertEquals(PaymentStatus.PENDING, payment.getStatus());

            // When & Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> payment.refund());

            assertTrue(exception.getMessage().contains("완료된 결제만 환불할 수 있습니다"));
        }

        @Test
        @DisplayName("FAILED 상태에서 refund() 호출 시 예외")
        void shouldThrowExceptionWhenFailed() {
            // Given
            Payment payment = createTestPayment();
            payment.fail();

            // When & Then
            assertThrows(IllegalStateException.class, () -> payment.refund());
        }

        @Test
        @DisplayName("이미 REFUNDED 상태에서 refund() 호출 시 예외")
        void shouldThrowExceptionWhenAlreadyRefunded() {
            // Given
            Payment payment = createTestPayment();
            payment.complete();
            payment.refund();

            // When & Then
            assertThrows(IllegalStateException.class, () -> payment.refund());
        }
    }

    @Nested
    @DisplayName("상태 전이 시나리오")
    class StateTransitionScenarioTest {

        @Test
        @DisplayName("정상 플로우: PENDING → COMPLETED → REFUNDED")
        void normalRefundFlow() {
            // Given
            Payment payment = createTestPayment();

            // When & Then
            assertEquals(PaymentStatus.PENDING, payment.getStatus());

            payment.complete();
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());

            payment.refund();
            assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        }

        @Test
        @DisplayName("실패 플로우: PENDING → FAILED")
        void failureFlow() {
            // Given
            Payment payment = createTestPayment();

            // When
            payment.fail();

            // Then
            assertEquals(PaymentStatus.FAILED, payment.getStatus());
        }
    }

    private Payment createTestPayment() {
        return Payment.create(
                Money.of(10000.0),
                Money.of(9000.0),
                Money.of(9900.0),
                Country.of("KR"),
                true);
    }
}
