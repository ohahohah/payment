package com.example.payment_ddd_v1_1;

import com.example.payment_ddd_v1_1.application.PaymentService;
import com.example.payment_ddd_v1_1.domain.model.Payment;
import com.example.payment_ddd_v1_1.domain.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Payment DDD V1_1 통합 테스트 (정석 DDD)
 *
 * ============================================================================
 * [테스트 목적]
 * ============================================================================
 *
 * 1. 전체 계층 통합 동작 검증
 *    - Controller → Service → Repository → DB
 *
 * 2. Domain ↔ JPA Entity 변환 검증
 *    - PaymentMapper를 통한 변환이 정확한지
 *
 * 3. JPA 영속화 검증
 *    - H2 In-Memory DB 사용
 *
 * ============================================================================
 * [payment_ddd_v1과의 차이점]
 * ============================================================================
 *
 * payment_ddd_v1:
 *   - Payment 자체가 JPA Entity
 *   - Repository가 Payment 직접 저장/조회
 *
 * payment_ddd_v1_1:
 *   - Payment는 순수 Java
 *   - PaymentJpaEntity로 변환 후 저장
 *   - 조회 후 Payment로 다시 변환
 *   - PaymentMapper가 변환 담당
 */
@SpringBootTest(classes = PaymentDddV1_1Application.class)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Payment DDD V1_1 통합 테스트 (정석 DDD)")
class PaymentDddV1_1IntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Nested
    @DisplayName("결제 생성 통합 테스트")
    class CreatePaymentIntegrationTest {

        @Test
        @DisplayName("일반 고객 결제 생성 및 조회")
        void createAndGetPayment() {
            // when
            Payment created = paymentService.createPayment(10000.0, "KR", false);
            Payment found = paymentService.getPayment(created.getId());

            // then
            assertThat(found.getId()).isNotNull();
            assertThat(found.getOriginalPrice().getAmount()).isEqualTo(10000);
            assertThat(found.getDiscountedAmount().getAmount()).isEqualTo(9500); // 5% 할인
            assertThat(found.getTaxedAmount().getAmount()).isEqualTo(10450);     // 10% 세금
            assertThat(found.getCountry().getCode()).isEqualTo("KR");
            assertThat(found.isVip()).isFalse();
            assertThat(found.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("VIP 고객 결제 생성")
        void createVipPayment() {
            // when
            Payment created = paymentService.createPayment(10000.0, "KR", true);

            // then
            assertThat(created.getDiscountedAmount().getAmount()).isEqualTo(9000); // 10% 할인
            assertThat(created.getTaxedAmount().getAmount()).isEqualTo(9900);    // 10% 세금
            assertThat(created.isVip()).isTrue();
        }

        @Test
        @DisplayName("Domain → JPA Entity → Domain 변환 검증")
        void verifyDomainEntityConversion() {
            // given
            Payment created = paymentService.createPayment(10000.0, "KR", false);

            // when - DB에서 다시 조회 (JPA Entity → Domain 변환)
            Payment found = paymentService.getPayment(created.getId());

            // then - 모든 데이터가 보존되었는지 확인
            assertThat(found.getId()).isEqualTo(created.getId());
            assertThat(found.getOriginalPrice()).isEqualTo(created.getOriginalPrice());
            assertThat(found.getDiscountedAmount()).isEqualTo(created.getDiscountedAmount());
            assertThat(found.getTaxedAmount()).isEqualTo(created.getTaxedAmount());
            assertThat(found.getCountry()).isEqualTo(created.getCountry());
            assertThat(found.isVip()).isEqualTo(created.isVip());
            assertThat(found.getStatus()).isEqualTo(created.getStatus());
        }
    }

    @Nested
    @DisplayName("결제 상태 변경 통합 테스트")
    class StatusChangeIntegrationTest {

        @Test
        @DisplayName("결제 완료 흐름")
        void completePaymentFlow() {
            // given
            Payment created = paymentService.createPayment(10000.0, "KR", false);
            assertThat(created.getStatus()).isEqualTo(PaymentStatus.PENDING);

            // when
            Payment completed = paymentService.completePayment(created.getId());

            // then
            assertThat(completed.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

            // 다시 조회해도 상태 유지 확인
            Payment found = paymentService.getPayment(created.getId());
            assertThat(found.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("환불 흐름")
        void refundPaymentFlow() {
            // given
            Payment created = paymentService.createPayment(10000.0, "KR", false);
            paymentService.completePayment(created.getId());

            // when
            Payment refunded = paymentService.refundPayment(created.getId());

            // then
            assertThat(refunded.getStatus()).isEqualTo(PaymentStatus.REFUNDED);

            // 다시 조회해도 상태 유지 확인
            Payment found = paymentService.getPayment(created.getId());
            assertThat(found.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("결제 실패 흐름")
        void failPaymentFlow() {
            // given
            Payment created = paymentService.createPayment(10000.0, "KR", false);

            // when
            Payment failed = paymentService.failPayment(created.getId());

            // then
            assertThat(failed.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("상태 전이가 DB에 반영되는지 확인")
        void verifyStatusPersistence() {
            // given
            Payment created = paymentService.createPayment(10000.0, "KR", false);

            // when - 상태 변경
            paymentService.completePayment(created.getId());

            // then - 조회 시 변경된 상태 확인
            Payment found = paymentService.getPayment(created.getId());
            assertThat(found.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("전체 조회 통합 테스트")
    class GetAllPaymentsIntegrationTest {

        @Test
        @DisplayName("여러 결제 생성 후 전체 조회")
        void getAllPayments() {
            // given
            paymentService.createPayment(10000.0, "KR", false);
            paymentService.createPayment(20000.0, "KR", true);
            paymentService.createPayment(30000.0, "US", false);

            // when
            List<Payment> payments = paymentService.getAllPayments();

            // then
            assertThat(payments).hasSize(3);
        }

        @Test
        @DisplayName("전체 조회 시 모든 데이터 보존 확인")
        void getAllPaymentsWithDataIntegrity() {
            // given
            paymentService.createPayment(10000.0, "KR", false);
            paymentService.createPayment(20000.0, "KR", true);

            // when
            List<Payment> payments = paymentService.getAllPayments();

            // then
            Payment regularPayment = payments.stream()
                    .filter(p -> !p.isVip())
                    .findFirst()
                    .orElseThrow();
            assertThat(regularPayment.getOriginalPrice().getAmount()).isEqualTo(10000);

            Payment vipPayment = payments.stream()
                    .filter(Payment::isVip)
                    .findFirst()
                    .orElseThrow();
            assertThat(vipPayment.getOriginalPrice().getAmount()).isEqualTo(20000);
        }
    }

    @Nested
    @DisplayName("예외 케이스 통합 테스트")
    class ExceptionIntegrationTest {

        @Test
        @DisplayName("존재하지 않는 결제 조회 시 예외")
        void getNotExistingPayment() {
            // when & then
            assertThatThrownBy(() -> paymentService.getPayment(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("결제를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("완료된 결제 다시 완료 시도 시 예외")
        void cannotCompleteAlreadyCompleted() {
            // given
            Payment created = paymentService.createPayment(10000.0, "KR", false);
            paymentService.completePayment(created.getId());

            // when & then
            assertThatThrownBy(() -> paymentService.completePayment(created.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("대기 상태 결제 환불 시도 시 예외")
        void cannotRefundPendingPayment() {
            // given
            Payment created = paymentService.createPayment(10000.0, "KR", false);

            // when & then
            assertThatThrownBy(() -> paymentService.refundPayment(created.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
