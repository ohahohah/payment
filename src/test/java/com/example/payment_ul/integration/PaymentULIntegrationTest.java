package com.example.payment_ul.integration;

import com.example.payment_ul.PaymentULApplication;
import com.example.payment_ul.dto.PaymentRequest;
import com.example.payment_ul.dto.PaymentResult;
import com.example.payment_ul.entity.Payment;
import com.example.payment_ul.entity.PaymentStatus;
import com.example.payment_ul.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

/**
 * PaymentULIntegrationTest - 유비쿼터스 랭귀지 결제 시스템 통합 테스트
 *
 * [독립 실행]
 * - PaymentULApplication을 사용하여 독립적으로 테스트합니다
 * - payment_ul 패키지의 빈만 로드됩니다
 * - Qualifier 없이 정상 동작을 검증합니다
 */
@SpringBootTest(classes = PaymentULApplication.class)
@Transactional
@DisplayName("유비쿼터스 랭귀지 결제 시스템 통합 테스트")
class PaymentULIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Nested
    @DisplayName("결제 처리 테스트")
    class PaymentProcessTest {

        @Test
        @DisplayName("VIP 고객 결제 - 15% 할인 + 10% 세금")
        void vipPaymentWithDiscountAndTax() {
            // given
            PaymentRequest request = new PaymentRequest(10000, "KR", true);

            // when
            PaymentResult result = paymentService.processPayment(request);

            // then
            // 10000 * 0.85 (VIP 15% 할인) = 8500
            // 8500 * 1.1 (VAT 10%) = 9350
            assertThat(result.originalPrice()).isEqualTo(10000);
            assertThat(result.discountedAmount()).isEqualTo(8500);
            assertThat(result.taxedAmount()).isEqualTo(9350);
        }

        @Test
        @DisplayName("일반 고객 결제 - 10% 할인 + 10% 세금")
        void normalPaymentWithDiscountAndTax() {
            // given
            PaymentRequest request = new PaymentRequest(10000, "KR", false);

            // when
            PaymentResult result = paymentService.processPayment(request);

            // then
            // 10000 * 0.90 (일반 10% 할인) = 9000
            // 9000 * 1.1 (VAT 10%) = 9900
            assertThat(result.originalPrice()).isEqualTo(10000);
            assertThat(result.discountedAmount()).isEqualTo(9000);
            assertThat(result.taxedAmount()).isEqualTo(9900);
        }
    }

    @Nested
    @DisplayName("결제 조회 테스트")
    class PaymentQueryTest {

        @Test
        @DisplayName("결제 조회 성공")
        void getPaymentSuccess() {
            // given
            PaymentRequest request = new PaymentRequest(10000, "KR", true);
            paymentService.processPayment(request);

            // when
            var payments = paymentService.getAllPayments();

            // then
            assertThat(payments).hasSize(1);
            assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("상태별 결제 조회")
        void getPaymentsByStatus() {
            // given
            paymentService.processPayment(new PaymentRequest(10000, "KR", true));
            paymentService.processPayment(new PaymentRequest(20000, "KR", false));

            // when
            var completedPayments = paymentService.getPaymentsByStatus(PaymentStatus.COMPLETED);

            // then
            assertThat(completedPayments).hasSize(2);
        }
    }

    @Nested
    @DisplayName("결제 환불 테스트")
    class PaymentRefundTest {

        @Test
        @DisplayName("완료된 결제 환불 성공")
        void refundCompletedPayment() {
            // given
            paymentService.processPayment(new PaymentRequest(10000, "KR", true));
            var payments = paymentService.getAllPayments();
            Long paymentId = payments.get(0).getId();

            // when
            Payment refundedPayment = paymentService.refundPayment(paymentId);

            // then
            assertThat(refundedPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("이미 환불된 결제는 다시 환불 불가")
        void cannotRefundAlreadyRefundedPayment() {
            // given
            paymentService.processPayment(new PaymentRequest(10000, "KR", true));
            var payments = paymentService.getAllPayments();
            Long paymentId = payments.get(0).getId();
            paymentService.refundPayment(paymentId);

            // when & then
            assertThatThrownBy(() -> paymentService.refundPayment(paymentId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("완료된 결제만 환불할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("유비쿼터스 랭귀지 검증")
    class UbiquitousLanguageTest {

        @Test
        @DisplayName("필드명이 유비쿼터스 랭귀지를 따름")
        void fieldNamesFollowUbiquitousLanguage() {
            // given
            PaymentRequest request = new PaymentRequest(10000, "KR", true);

            // when
            PaymentResult result = paymentService.processPayment(request);

            // then - 유비쿼터스 랭귀지 필드명 검증
            assertThat(result.originalPrice()).isNotNull();       // amt1 → originalPrice
            assertThat(result.discountedAmount()).isNotNull();    // amt2 → discountedAmount
            assertThat(result.taxedAmount()).isNotNull();         // amt3 → taxedAmount
            assertThat(result.country()).isNotNull();             // cd → country
        }
    }
}
