package com.example.payment_ddd_v1;

import com.example.payment_ddd_v1.application.PaymentService;
import com.example.payment_ddd_v1.domain.model.Payment;
import com.example.payment_ddd_v1.domain.model.PaymentStatus;
import com.example.payment_ddd_v1.interfaces.PaymentRequest;
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
 * Payment DDD V1 통합 테스트
 *
 * [테스트 목적]
 * - 전체 계층 통합 동작 검증
 * - 실제 DB(H2) 연동 테스트
 * - JPA 영속화 검증
 *
 * [테스트 환경]
 * - Spring Boot Test 컨텍스트
 * - H2 In-Memory DB
 * - @Transactional: 각 테스트 후 롤백
 */
@SpringBootTest(classes = PaymentDddV1Application.class)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Payment DDD V1 통합 테스트")
class PaymentDddV1IntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Nested
    @DisplayName("결제 생성 통합 테스트")
    class CreatePaymentIntegrationTest {

        @Test
        @DisplayName("일반 고객 결제 생성 및 조회")
        void createAndGetPayment() {
            // given
            PaymentRequest request = new PaymentRequest();
            request.setPrice(10000.0);
            request.setCountryCode("KR");
            request.setIsVip(false);

            // when
            Payment created = paymentService.createPayment(request);
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
            // given
            PaymentRequest request = new PaymentRequest();
            request.setPrice(10000.0);
            request.setCountryCode("KR");
            request.setIsVip(true);

            // when
            Payment created = paymentService.createPayment(request);

            // then
            assertThat(created.getDiscountedAmount().getAmount()).isEqualTo(9000); // 10% 할인
            assertThat(created.getTaxedAmount().getAmount()).isEqualTo(9900);    // 10% 세금
            assertThat(created.isVip()).isTrue();
        }
    }

    @Nested
    @DisplayName("결제 상태 변경 통합 테스트")
    class StatusChangeIntegrationTest {

        @Test
        @DisplayName("결제 완료 흐름")
        void completePaymentFlow() {
            // given
            PaymentRequest request = new PaymentRequest();
            request.setPrice(10000.0);
            request.setCountryCode("KR");
            request.setIsVip(false);

            Payment created = paymentService.createPayment(request);
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
            PaymentRequest request = new PaymentRequest();
            request.setPrice(10000.0);
            request.setCountryCode("KR");
            request.setIsVip(false);

            Payment created = paymentService.createPayment(request);
            paymentService.completePayment(created.getId());

            // when
            Payment refunded = paymentService.refundPayment(created.getId());

            // then
            assertThat(refunded.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("결제 실패 흐름")
        void failPaymentFlow() {
            // given
            PaymentRequest request = new PaymentRequest();
            request.setPrice(10000.0);
            request.setCountryCode("KR");
            request.setIsVip(false);

            Payment created = paymentService.createPayment(request);

            // when
            Payment failed = paymentService.failPayment(created.getId());

            // then
            assertThat(failed.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("전체 조회 통합 테스트")
    class GetAllPaymentsIntegrationTest {

        @Test
        @DisplayName("여러 결제 생성 후 전체 조회")
        void getAllPayments() {
            // given
            PaymentRequest request1 = new PaymentRequest();
            request1.setPrice(10000.0);
            request1.setCountryCode("KR");
            request1.setIsVip(false);

            PaymentRequest request2 = new PaymentRequest();
            request2.setPrice(20000.0);
            request2.setCountryCode("KR");
            request2.setIsVip(true);

            paymentService.createPayment(request1);
            paymentService.createPayment(request2);

            // when
            List<Payment> payments = paymentService.getAllPayments();

            // then
            assertThat(payments).hasSize(2);
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
            PaymentRequest request = new PaymentRequest();
            request.setPrice(10000.0);
            request.setCountryCode("KR");
            request.setIsVip(false);

            Payment created = paymentService.createPayment(request);
            paymentService.completePayment(created.getId());

            // when & then
            assertThatThrownBy(() -> paymentService.completePayment(created.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
