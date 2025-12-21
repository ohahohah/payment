package com.example.payment_ddd.domain.model;

import com.example.payment_ddd.domain.event.DomainEvent;
import com.example.payment_ddd.domain.event.PaymentCompletedEvent;
import com.example.payment_ddd.domain.event.PaymentRefundedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PaymentTest - Payment Aggregate Root 단위 테스트
 *
 * [테스트 포인트]
 * 1. Rich Domain Model: 비즈니스 로직이 엔티티 내부에 있는지
 * 2. 상태 전이 규칙: 유효하지 않은 상태 변경 거부
 * 3. 도메인 이벤트: 상태 변경 시 이벤트 등록
 */
@DisplayName("Payment Aggregate Root 테스트")
class PaymentTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("결제 생성 시 PENDING 상태")
        void createWithPendingStatus() {
            Payment payment = createSamplePayment();

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("결제 생성 시 금액 정보 보유")
        void createWithAmounts() {
            Money original = Money.of(10000);
            Money discounted = Money.of(9000);
            Money taxed = Money.of(9900);

            Payment payment = Payment.create(original, discounted, taxed, Country.korea(), true);

            assertThat(payment.getOriginalPrice()).isEqualTo(original);
            assertThat(payment.getDiscountedAmount()).isEqualTo(discounted);
            assertThat(payment.getTaxedAmount()).isEqualTo(taxed);
        }
    }

    @Nested
    @DisplayName("결제 완료 테스트")
    class CompleteTest {

        @Test
        @DisplayName("PENDING 상태에서 완료 가능")
        void completeFromPending() {
            Payment payment = createSamplePayment();

            payment.complete();

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("완료 시 PaymentCompletedEvent 발생")
        void emitEventOnComplete() {
            Payment payment = createSamplePayment();

            payment.complete();

            List<DomainEvent> events = payment.getDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(PaymentCompletedEvent.class);
        }

        @Test
        @DisplayName("이미 완료된 결제는 다시 완료 불가")
        void cannotCompleteAlreadyCompleted() {
            Payment payment = createSamplePayment();
            payment.complete();

            assertThatThrownBy(() -> payment.complete())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("대기 상태");
        }
    }

    @Nested
    @DisplayName("결제 실패 테스트")
    class FailTest {

        @Test
        @DisplayName("PENDING 상태에서 실패 가능")
        void failFromPending() {
            Payment payment = createSamplePayment();

            payment.fail();

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("완료된 결제는 실패 처리 불가")
        void cannotFailCompleted() {
            Payment payment = createSamplePayment();
            payment.complete();

            assertThatThrownBy(() -> payment.fail())
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("환불 테스트")
    class RefundTest {

        @Test
        @DisplayName("완료된 결제만 환불 가능")
        void refundFromCompleted() {
            Payment payment = createSamplePayment();
            payment.complete();
            payment.pullDomainEvents(); // 이전 이벤트 제거

            payment.refund();

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("환불 시 PaymentRefundedEvent 발생")
        void emitEventOnRefund() {
            Payment payment = createSamplePayment();
            payment.complete();
            payment.pullDomainEvents();

            payment.refund();

            List<DomainEvent> events = payment.getDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(PaymentRefundedEvent.class);
        }

        @Test
        @DisplayName("대기 상태는 환불 불가")
        void cannotRefundPending() {
            Payment payment = createSamplePayment();

            assertThatThrownBy(() -> payment.refund())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("완료된 결제");
        }

        @Test
        @DisplayName("실패한 결제는 환불 불가")
        void cannotRefundFailed() {
            Payment payment = createSamplePayment();
            payment.fail();

            assertThatThrownBy(() -> payment.refund())
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("도메인 이벤트 테스트")
    class DomainEventTest {

        @Test
        @DisplayName("pullDomainEvents 호출 시 이벤트 반환 및 초기화")
        void pullEventsAndClear() {
            Payment payment = createSamplePayment();
            payment.complete();

            List<DomainEvent> events = payment.pullDomainEvents();
            List<DomainEvent> eventsAgain = payment.pullDomainEvents();

            assertThat(events).hasSize(1);
            assertThat(eventsAgain).isEmpty();
        }
    }

    private Payment createSamplePayment() {
        return Payment.create(
                Money.of(10000),
                Money.of(9000),
                Money.of(9900),
                Country.korea(),
                true
        );
    }
}
