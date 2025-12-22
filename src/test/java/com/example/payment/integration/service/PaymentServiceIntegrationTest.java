package com.example.payment.integration.service;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResult;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PaymentService 통합 테스트
 */
@SpringBootTest
@Transactional
@DisplayName("PaymentService 통합 테스트")
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
    }

    @Nested
    @DisplayName("결제 처리 통합 테스트")
    class ProcessPaymentTest {

        @Test
        @DisplayName("결제 요청이 정상 처리되어 DB에 저장된다")
        void shouldProcessAndSavePayment() {
            // Given
            PaymentRequest request = new PaymentRequest(10000, "KR", true);

            // When
            PaymentResult result = paymentService.execute(request);

            // Then
            assertThat(result.amt1()).isEqualTo(10000);
            assertThat(result.amt2()).isEqualTo(8500);
            assertThat(result.amt3()).isEqualTo(9350);

            // DB 저장 검증
            List<Payment> payments = paymentRepository.findAll();
            assertThat(payments)
                    .hasSize(1)
                    .first()
                    .satisfies(payment -> {
                        assertThat(payment.getStat()).isEqualTo(PaymentStatus.C);
                        assertThat(payment.getAmt3()).isEqualTo(9350);
                    });
        }

        @Test
        @DisplayName("VIP 고객은 15% 할인이 적용된다")
        void shouldApplyVipDiscount() {
            // Given
            PaymentRequest request = new PaymentRequest(10000, "KR", true);

            // When
            PaymentResult result = paymentService.execute(request);

            // Then
            assertThat(result.amt2()).isEqualTo(8500);
        }

        @Test
        @DisplayName("일반 고객은 10% 할인이 적용된다")
        void shouldApplyNormalDiscount() {
            // Given
            PaymentRequest request = new PaymentRequest(10000, "KR", false);

            // When
            PaymentResult result = paymentService.execute(request);

            // Then
            assertThat(result.amt2()).isEqualTo(9000);
        }

        @Test
        @DisplayName("한국 결제는 10% 부가세가 적용된다")
        void shouldApplyKoreaTax() {
            // Given
            PaymentRequest request = new PaymentRequest(10000, "KR", true);

            // When
            PaymentResult result = paymentService.execute(request);

            // Then
            double expectedTax = 8500 * 1.1;
            assertThat(result.amt3()).isEqualTo(expectedTax);
        }
    }

    @Nested
    @DisplayName("결제 조회 통합 테스트")
    class GetPaymentTest {

        @Test
        @DisplayName("존재하는 결제를 ID로 조회할 수 있다")
        void shouldFindPaymentById() {
            // Given
            PaymentRequest request = new PaymentRequest(10000, "KR", true);
            paymentService.execute(request);
            Payment savedPayment = paymentRepository.findAll().get(0);

            // When
            Payment found = paymentService.getData(savedPayment.getId());

            // Then
            assertThat(found.getId()).isEqualTo(savedPayment.getId());
            assertThat(found.getAmt1()).isEqualTo(10000);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
        void shouldThrowExceptionForNonExistentId() {
            // Given
            Long nonExistentId = 9999L;

            // When & Then
            assertThatThrownBy(() -> paymentService.getData(nonExistentId))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("전체 결제 목록을 조회할 수 있다")
        void shouldFindAllPayments() {
            // Given
            paymentService.execute(new PaymentRequest(10000, "KR", true));
            paymentService.execute(new PaymentRequest(20000, "US", false));
            paymentService.execute(new PaymentRequest(30000, "KR", true));

            // When
            List<Payment> payments = paymentService.getList();

            // Then
            assertThat(payments).hasSize(3);
        }

        @Test
        @DisplayName("상태별로 결제를 조회할 수 있다")
        void shouldFindPaymentsByStatus() {
            // Given
            paymentService.execute(new PaymentRequest(10000, "KR", true));
            paymentService.execute(new PaymentRequest(20000, "KR", false));
            paymentService.execute(new PaymentRequest(30000, "US", true));

            // When
            List<Payment> completedPayments = paymentService.getListByStat(PaymentStatus.C);
            List<Payment> pendingPayments = paymentService.getListByStat(PaymentStatus.P);

            // Then
            assertThat(completedPayments).hasSize(3);
            assertThat(pendingPayments).isEmpty();
        }
    }

    @Nested
    @DisplayName("결제 환불 통합 테스트")
    class RefundPaymentTest {

        @Test
        @DisplayName("완료된 결제를 환불할 수 있다")
        void shouldRefundCompletedPayment() {
            // Given
            paymentService.execute(new PaymentRequest(10000, "KR", true));
            Payment payment = paymentRepository.findAll().get(0);
            assertThat(payment.getStat()).isEqualTo(PaymentStatus.C);

            // When
            Payment refunded = paymentService.updateStatus(payment.getId());

            // Then
            assertThat(refunded.getStat()).isEqualTo(PaymentStatus.R);

            // DB 검증
            Payment fromDb = paymentRepository.findById(payment.getId()).orElseThrow();
            assertThat(fromDb.getStat()).isEqualTo(PaymentStatus.R);
        }

        @Test
        @DisplayName("존재하지 않는 결제는 환불할 수 없다")
        void shouldNotRefundNonExistentPayment() {
            // Given
            Long nonExistentId = 9999L;

            // When & Then
            assertThatThrownBy(() -> paymentService.updateStatus(nonExistentId))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("전체 플로우 테스트")
    class EndToEndFlowTest {

        @Test
        @DisplayName("결제 생성 → 조회 → 환불 전체 플로우")
        void completePaymentFlow() {
            // 1. 결제 생성
            PaymentRequest request = new PaymentRequest(50000, "KR", true);
            PaymentResult result = paymentService.execute(request);

            assertThat(result.amt1()).isEqualTo(50000);
            assertThat(result.amt2()).isEqualTo(42500);
            assertThat(result.amt3()).isEqualTo(46750);

            // 2. 결제 조회
            Payment payment = paymentRepository.findAll().get(0);
            Payment found = paymentService.getData(payment.getId());

            assertThat(found.getStat()).isEqualTo(PaymentStatus.C);

            // 3. 환불
            Payment refunded = paymentService.updateStatus(payment.getId());

            assertThat(refunded.getStat()).isEqualTo(PaymentStatus.R);

            // 4. 상태별 조회로 검증
            List<Payment> completed = paymentService.getListByStat(PaymentStatus.C);
            List<Payment> refundedList = paymentService.getListByStat(PaymentStatus.R);

            assertThat(completed).isEmpty();
            assertThat(refundedList).hasSize(1);
        }
    }
}
