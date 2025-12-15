package com.example.payment.unit.entity;

import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ============================================================================
 * [GOOD] PaymentEntityTest - 결제 엔티티 단위 테스트
 * ============================================================================
 *
 * [테스트 특징]
 * - JPA/DB 없이 순수 도메인 로직만 테스트
 * - 엔티티의 생성, 상태 변경, 비즈니스 규칙 검증
 * - 스프링 컨텍스트 불필요 → 빠른 실행
 *
 * [테스트 범위]
 * - 정적 팩토리 메서드 (Payment.create)
 * - 상태 변경 메서드 (complete, fail, refund)
 * - 비즈니스 규칙 (환불 조건 등)
 */
@DisplayName("Payment 엔티티 단위 테스트")
class PaymentEntityTest {

    // 테스트 데이터 상수
    private static final Double ORIGINAL_PRICE = 10000.0;
    private static final Double DISCOUNTED_AMOUNT = 8500.0;
    private static final Double TAXED_AMOUNT = 9350.0;
    private static final String COUNTRY_KR = "KR";
    private static final Boolean IS_VIP = true;

    @Nested
    @DisplayName("결제 생성 테스트")
    class PaymentCreationTest {

        @Test
        @DisplayName("정적 팩토리 메서드로 결제를 생성할 수 있다")
        void shouldCreatePaymentWithFactoryMethod() {
            // When
            Payment payment = Payment.create(
                    ORIGINAL_PRICE,
                    DISCOUNTED_AMOUNT,
                    TAXED_AMOUNT,
                    COUNTRY_KR,
                    IS_VIP
            );

            // Then
            assertThat(payment.getOriginalPrice()).isEqualTo(ORIGINAL_PRICE);
            assertThat(payment.getDiscountedAmount()).isEqualTo(DISCOUNTED_AMOUNT);
            assertThat(payment.getTaxedAmount()).isEqualTo(TAXED_AMOUNT);
            assertThat(payment.getCountry()).isEqualTo(COUNTRY_KR);
            assertThat(payment.getIsVip()).isEqualTo(IS_VIP);
        }

        @Test
        @DisplayName("생성된 결제의 초기 상태는 PENDING이다")
        void newPaymentShouldHavePendingStatus() {
            // When
            Payment payment = createTestPayment();

            // Then
            assertThat(payment.getStatus())
                    .as("새로 생성된 결제는 PENDING 상태여야 합니다")
                    .isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("생성된 결제에 생성 시간이 설정된다")
        void newPaymentShouldHaveCreatedAt() {
            // When
            Payment payment = createTestPayment();

            // Then
            assertThat(payment.getCreatedAt())
                    .as("생성 시간이 설정되어야 합니다")
                    .isNotNull();
        }

        @Test
        @DisplayName("생성된 결제에 수정 시간이 설정된다")
        void newPaymentShouldHaveUpdatedAt() {
            // When
            Payment payment = createTestPayment();

            // Then
            assertThat(payment.getUpdatedAt())
                    .as("수정 시간이 설정되어야 합니다")
                    .isNotNull();
        }
    }

    @Nested
    @DisplayName("결제 완료 테스트")
    class PaymentCompletionTest {

        @Test
        @DisplayName("PENDING 상태의 결제를 완료할 수 있다")
        void shouldCompletePayment() {
            // Given
            Payment payment = createTestPayment();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

            // When
            payment.complete();

            // Then
            assertThat(payment.getStatus())
                    .as("완료 처리 후 COMPLETED 상태여야 합니다")
                    .isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("완료 처리 시 수정 시간이 갱신된다")
        void completeShouldUpdateTimestamp() {
            // Given
            Payment payment = createTestPayment();
            var originalUpdatedAt = payment.getUpdatedAt();

            // 시간 차이를 만들기 위해 짧은 대기
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}

            // When
            payment.complete();

            // Then
            assertThat(payment.getUpdatedAt())
                    .as("수정 시간이 갱신되어야 합니다")
                    .isAfterOrEqualTo(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("결제 실패 테스트")
    class PaymentFailureTest {

        @Test
        @DisplayName("PENDING 상태의 결제를 실패 처리할 수 있다")
        void shouldFailPayment() {
            // Given
            Payment payment = createTestPayment();

            // When
            payment.fail();

            // Then
            assertThat(payment.getStatus())
                    .as("실패 처리 후 FAILED 상태여야 합니다")
                    .isEqualTo(PaymentStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("결제 환불 테스트")
    class PaymentRefundTest {

        @Test
        @DisplayName("완료된 결제를 환불할 수 있다")
        void shouldRefundCompletedPayment() {
            // Given
            Payment payment = createTestPayment();
            payment.complete();  // 먼저 완료 처리
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

            // When
            payment.refund();

            // Then
            assertThat(payment.getStatus())
                    .as("환불 처리 후 REFUNDED 상태여야 합니다")
                    .isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("PENDING 상태의 결제는 환불할 수 없다")
        void shouldNotRefundPendingPayment() {
            // Given
            Payment payment = createTestPayment();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

            // When & Then
            assertThatThrownBy(() -> payment.refund())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("완료된 결제만 환불");
        }

        @Test
        @DisplayName("실패한 결제는 환불할 수 없다")
        void shouldNotRefundFailedPayment() {
            // Given
            Payment payment = createTestPayment();
            payment.fail();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);

            // When & Then
            assertThatThrownBy(() -> payment.refund())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("완료된 결제만 환불");
        }

        @Test
        @DisplayName("이미 환불된 결제는 다시 환불할 수 없다")
        void shouldNotRefundAlreadyRefundedPayment() {
            // Given
            Payment payment = createTestPayment();
            payment.complete();
            payment.refund();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);

            // When & Then
            assertThatThrownBy(() -> payment.refund())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("완료된 결제만 환불");
        }
    }

    @Nested
    @DisplayName("상태 전이 테스트")
    class StatusTransitionTest {

        @Test
        @DisplayName("정상적인 결제 플로우: PENDING → COMPLETED → REFUNDED")
        void normalPaymentFlow() {
            // Given
            Payment payment = createTestPayment();

            // When & Then: PENDING → COMPLETED
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            payment.complete();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

            // When & Then: COMPLETED → REFUNDED
            payment.refund();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("실패 플로우: PENDING → FAILED")
        void failedPaymentFlow() {
            // Given
            Payment payment = createTestPayment();

            // When & Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            payment.fail();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }

    // ========================================================================
    // 테스트 헬퍼 메서드
    // ========================================================================

    /**
     * 테스트용 Payment 객체 생성 헬퍼
     * - 반복되는 객체 생성 코드를 추출
     * - 테스트 코드 가독성 향상
     */
    private Payment createTestPayment() {
        return Payment.create(
                ORIGINAL_PRICE,
                DISCOUNTED_AMOUNT,
                TAXED_AMOUNT,
                COUNTRY_KR,
                IS_VIP
        );
    }
}
