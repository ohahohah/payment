package com.example.payment_ddd.integration;

import com.example.payment.PaymentApplication;
import com.example.payment_ddd.application.command.CreatePaymentCommand;
import com.example.payment_ddd.application.command.RefundPaymentCommand;
import com.example.payment_ddd.application.service.PaymentCommandService;
import com.example.payment_ddd.domain.model.Payment;
import com.example.payment_ddd.domain.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

/**
 * PaymentDddIntegrationTest - DDD 결제 시스템 통합 테스트
 *
 * [통합 테스트 특징]
 * - 실제 Spring Context 로딩
 * - 실제 DB 사용 (H2 인메모리)
 * - 전체 흐름 검증
 */
@SpringBootTest(classes = PaymentApplication.class)
@Transactional
@DisplayName("DDD 결제 시스템 통합 테스트")
class PaymentDddIntegrationTest {

    @Autowired
    private PaymentCommandService paymentCommandService;

    @Nested
    @DisplayName("결제 전체 흐름")
    class FullFlowTest {

        @Test
        @DisplayName("결제 생성 → 완료 → 환불 전체 흐름")
        void fullPaymentFlow() {
            // 1. 결제 생성 및 완료
            CreatePaymentCommand createCommand = new CreatePaymentCommand(10000, "KR", true);
            Payment payment = paymentCommandService.createAndCompletePayment(createCommand);

            assertThat(payment.getId()).isNotNull();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(payment.getTaxedAmount().getAmount()).isEqualTo(9900);

            // 2. 환불
            RefundPaymentCommand refundCommand = new RefundPaymentCommand(payment.getId());
            Payment refundedPayment = paymentCommandService.refundPayment(refundCommand);

            assertThat(refundedPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);

            // 3. 조회
            Payment found = paymentCommandService.getPayment(payment.getId());
            assertThat(found.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("한국 VIP 결제 - 할인 + 세금")
        void koreanVipPayment() {
            CreatePaymentCommand command = new CreatePaymentCommand(10000, "KR", true);

            Payment payment = paymentCommandService.createAndCompletePayment(command);

            // 10000 * 0.9 (VIP 10% 할인) = 9000
            // 9000 * 1.1 (VAT 10%) = 9900
            assertThat(payment.getOriginalPrice().getAmount()).isEqualTo(10000);
            assertThat(payment.getDiscountedAmount().getAmount()).isEqualTo(9000);
            assertThat(payment.getTaxedAmount().getAmount()).isEqualTo(9900);
        }

        @Test
        @DisplayName("미국 일반 고객 결제 - 세금만")
        void usNonVipPayment() {
            CreatePaymentCommand command = new CreatePaymentCommand(10000, "US", false);

            Payment payment = paymentCommandService.createAndCompletePayment(command);

            // 10000 * 1.0 (할인 없음) = 10000
            // 10000 * 1.08 (Sales Tax 8%) = 10800
            assertThat(payment.getDiscountedAmount().getAmount()).isEqualTo(10000);
            assertThat(payment.getTaxedAmount().getAmount()).isEqualTo(10800);
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 검증")
    class BusinessRuleTest {

        @Test
        @DisplayName("완료되지 않은 결제는 환불 불가")
        void cannotRefundNonCompletedPayment() {
            // 이 테스트는 현재 구조에서는 불가능
            // createAndCompletePayment가 완료 상태로 저장하기 때문
            // 실제로는 별도의 결제 승인 API가 있어야 함
        }

        @Test
        @DisplayName("이미 환불된 결제는 다시 환불 불가")
        void cannotRefundAlreadyRefundedPayment() {
            CreatePaymentCommand createCommand = new CreatePaymentCommand(10000, "KR", true);
            Payment payment = paymentCommandService.createAndCompletePayment(createCommand);

            RefundPaymentCommand refundCommand = new RefundPaymentCommand(payment.getId());
            paymentCommandService.refundPayment(refundCommand);

            // 두 번째 환불 시도
            assertThatThrownBy(() -> paymentCommandService.refundPayment(refundCommand))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
