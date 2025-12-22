package com.example.payment.unit.entity;

import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Payment 엔티티 단위 테스트
 */
@DisplayName("Payment 엔티티 단위 테스트")
class PaymentEntityTest {

    private static final Double AMT1 = 10000.0;     // 원래 가격
    private static final Double AMT2 = 8500.0;      // 할인 후
    private static final Double AMT3 = 9350.0;      // 세금 후
    private static final String CD = "KR";          // 국가 코드
    private static final Boolean FLAG = true;       // VIP 여부

    @Nested
    @DisplayName("결제 생성 테스트")
    class PaymentCreationTest {

        @Test
        @DisplayName("정적 팩토리 메서드로 결제를 생성할 수 있다")
        void shouldCreatePaymentWithFactoryMethod() {
            // When
            Payment payment = Payment.create(AMT1, AMT2, AMT3, CD, FLAG);

            // Then
            assertThat(payment.getAmt1()).isEqualTo(AMT1);
            assertThat(payment.getAmt2()).isEqualTo(AMT2);
            assertThat(payment.getAmt3()).isEqualTo(AMT3);
            assertThat(payment.getCd()).isEqualTo(CD);
            assertThat(payment.getFlag()).isEqualTo(FLAG);
        }

        @Test
        @DisplayName("생성된 결제의 초기 상태는 P이다")
        void newPaymentShouldHavePendingStatus() {
            // When
            Payment payment = createTestPayment();

            // Then
            assertThat(payment.getStat()).isEqualTo(PaymentStatus.P);
        }

        @Test
        @DisplayName("생성된 결제에 생성 시간이 설정된다")
        void newPaymentShouldHaveCreatedAt() {
            // When
            Payment payment = createTestPayment();

            // Then
            assertThat(payment.getCdt()).isNotNull();
        }

        @Test
        @DisplayName("생성된 결제에 수정 시간이 설정된다")
        void newPaymentShouldHaveUpdatedAt() {
            // When
            Payment payment = createTestPayment();

            // Then
            assertThat(payment.getUdt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Setter 테스트")
    class SetterTest {

        @Test
        @DisplayName("setStat로 상태를 변경할 수 있다")
        void shouldChangeStatusWithSetter() {
            // Given
            Payment payment = createTestPayment();

            // When
            payment.setStat(PaymentStatus.C);

            // Then
            assertThat(payment.getStat()).isEqualTo(PaymentStatus.C);
        }

        @Test
        @DisplayName("setUdt로 수정 시간을 변경할 수 있다")
        void shouldChangeUpdatedAtWithSetter() {
            // Given
            Payment payment = createTestPayment();
            LocalDateTime newTime = LocalDateTime.now().plusHours(1);

            // When
            payment.setUdt(newTime);

            // Then
            assertThat(payment.getUdt()).isEqualTo(newTime);
        }
    }

    @Nested
    @DisplayName("상태 변경 테스트")
    class StatusChangeTest {

        @Test
        @DisplayName("P에서 C로 변경할 수 있다")
        void shouldChangeFromPendingToCompleted() {
            // Given
            Payment payment = createTestPayment();

            // When
            payment.setStat(PaymentStatus.C);

            // Then
            assertThat(payment.getStat()).isEqualTo(PaymentStatus.C);
        }

        @Test
        @DisplayName("C에서 R로 변경할 수 있다")
        void shouldChangeFromCompletedToRefunded() {
            // Given
            Payment payment = createTestPayment();
            payment.setStat(PaymentStatus.C);

            // When
            payment.setStat(PaymentStatus.R);

            // Then
            assertThat(payment.getStat()).isEqualTo(PaymentStatus.R);
        }

        @Test
        @DisplayName("P에서 F로 변경할 수 있다")
        void shouldChangeFromPendingToFailed() {
            // Given
            Payment payment = createTestPayment();

            // When
            payment.setStat(PaymentStatus.F);

            // Then
            assertThat(payment.getStat()).isEqualTo(PaymentStatus.F);
        }
    }

    private Payment createTestPayment() {
        return Payment.create(AMT1, AMT2, AMT3, CD, FLAG);
    }
}
