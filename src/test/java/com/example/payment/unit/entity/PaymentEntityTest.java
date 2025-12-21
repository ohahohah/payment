package com.example.payment.unit.entity;

import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ============================================================================
 * [GOOD] PaymentEntityTest - 결제 엔티티 단위 테스트
 * ============================================================================
 *
 * [테스트 특징]
 * - JPA/DB 없이 순수 엔티티만 테스트
 * - 엔티티의 생성, getter/setter 검증
 * - 스프링 컨텍스트 불필요 → 빠른 실행
 *
 * [테스트 범위]
 * - 정적 팩토리 메서드 (Payment.create)
 * - Getter/Setter 메서드
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
    @DisplayName("Setter 테스트")
    class SetterTest {

        @Test
        @DisplayName("setStatus로 상태를 변경할 수 있다")
        void shouldChangeStatusWithSetter() {
            // Given
            Payment payment = createTestPayment();

            // When
            payment.setStatus(PaymentStatus.COMPLETED);

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("setUpdatedAt로 수정 시간을 변경할 수 있다")
        void shouldChangeUpdatedAtWithSetter() {
            // Given
            Payment payment = createTestPayment();
            LocalDateTime newTime = LocalDateTime.now().plusHours(1);

            // When
            payment.setUpdatedAt(newTime);

            // Then
            assertThat(payment.getUpdatedAt()).isEqualTo(newTime);
        }
    }

    @Nested
    @DisplayName("상태 변경 테스트")
    class StatusChangeTest {

        @Test
        @DisplayName("PENDING에서 COMPLETED로 변경할 수 있다")
        void shouldChangeFromPendingToCompleted() {
            // Given
            Payment payment = createTestPayment();

            // When
            payment.setStatus(PaymentStatus.COMPLETED);

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("COMPLETED에서 REFUNDED로 변경할 수 있다")
        void shouldChangeFromCompletedToRefunded() {
            // Given
            Payment payment = createTestPayment();
            payment.setStatus(PaymentStatus.COMPLETED);

            // When
            payment.setStatus(PaymentStatus.REFUNDED);

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("PENDING에서 FAILED로 변경할 수 있다")
        void shouldChangeFromPendingToFailed() {
            // Given
            Payment payment = createTestPayment();

            // When
            payment.setStatus(PaymentStatus.FAILED);

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }

    // ========================================================================
    // 테스트 헬퍼 메서드
    // ========================================================================

    /**
     * 테스트용 Payment 객체 생성 헬퍼
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
