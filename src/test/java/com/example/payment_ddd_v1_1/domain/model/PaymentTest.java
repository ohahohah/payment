package com.example.payment_ddd_v1_1.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Payment Entity 단위 테스트 (순수 Java - JPA 없음!)
 *
 * ============================================================================
 * [정석 DDD 테스트의 핵심 장점]
 * ============================================================================
 *
 * 1. JPA 없이 순수 Java만으로 테스트
 *    - @Entity 어노테이션 없음
 *    - Spring Context 불필요
 *    - DB 연결 불필요
 *
 * 2. 빠른 실행
 *    - 밀리초 단위 실행
 *    - CI/CD 파이프라인에서 빠른 피드백
 *
 * 3. 도메인 로직 집중
 *    - 인프라 걱정 없이 비즈니스 규칙만 테스트
 *
 * ============================================================================
 * [payment_ddd_v1과의 차이점]
 * ============================================================================
 *
 * payment_ddd_v1:
 *   - Payment에 @Entity 있음
 *   - protected 기본 생성자 필요 (JPA용)
 *
 * payment_ddd_v1_1:
 *   - Payment는 순수 Java
 *   - create(), reconstitute() 팩토리 메서드로 생성
 *   - JPA 매핑은 PaymentJpaEntity가 담당
 */
@DisplayName("Payment Entity 테스트 (순수 Java - JPA 없음)")
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
        @DisplayName("create() - 새 결제 생성")
        void createNewPayment() {
            // when
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, false);

            // then
            assertThat(payment.getId()).isNull(); // 저장 전이므로 ID 없음
            assertThat(payment.getOriginalPrice()).isEqualTo(originalPrice);
            assertThat(payment.getDiscountedAmount()).isEqualTo(discountedAmount);
            assertThat(payment.getTaxedAmount()).isEqualTo(taxedAmount);
            assertThat(payment.getCountry()).isEqualTo(country);
            assertThat(payment.isVip()).isFalse();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getCreatedAt()).isNotNull();
            assertThat(payment.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("create() - VIP 결제 생성")
        void createVipPayment() {
            // when
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, true);

            // then
            assertThat(payment.isVip()).isTrue();
        }

        @Test
        @DisplayName("reconstitute() - 기존 결제 복원 (Repository용)")
        void reconstituteExistingPayment() {
            // given
            LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
            LocalDateTime updatedAt = LocalDateTime.now();

            // when
            Payment payment = Payment.reconstitute(
                    1L,
                    originalPrice,
                    discountedAmount,
                    taxedAmount,
                    country,
                    false,
                    PaymentStatus.COMPLETED,
                    createdAt,
                    updatedAt
            );

            // then
            assertThat(payment.getId()).isEqualTo(1L);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(payment.getCreatedAt()).isEqualTo(createdAt);
            assertThat(payment.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    @DisplayName("ID 할당 테스트")
    class AssignIdTest {

        @Test
        @DisplayName("ID 할당 성공")
        void assignId() {
            // given
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, false);

            // when
            payment.assignId(100L);

            // then
            assertThat(payment.getId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("ID 중복 할당 시 예외")
        void cannotAssignIdTwice() {
            // given
            Payment payment = Payment.create(
                    originalPrice, discountedAmount, taxedAmount, country, false);
            payment.assignId(100L);

            // when & then
            assertThatThrownBy(() -> payment.assignId(200L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("한 번만");
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
