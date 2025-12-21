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
 * ============================================================================
 * [GOOD] PaymentServiceIntegrationTest - 서비스 통합 테스트
 * ============================================================================
 *
 * [@SpringBootTest]
 * - 전체 스프링 컨텍스트를 로딩하는 통합 테스트
 * - 실제 환경과 유사하게 모든 빈이 주입됨
 * - 가장 무거운 테스트이므로 꼭 필요한 경우에만 사용
 *
 * [언제 @SpringBootTest를 사용하나요?]
 * 1. 전체 플로우 검증 (E2E)
 * 2. 여러 레이어 간 상호작용 검증
 * 3. 트랜잭션 동작 검증
 * 4. 실제 DB 연동 테스트
 *
 * [@Transactional]
 * - 각 테스트를 트랜잭션으로 감싸고 테스트 후 롤백
 * - 테스트 간 데이터 격리 보장
 * - 주의: 실제 트랜잭션 동작과 다를 수 있음 (전파 레벨 등)
 *
 * [통합 테스트 vs 단위 테스트]
 * - 단위 테스트: 개별 컴포넌트 검증, 빠름, 격리됨
 * - 통합 테스트: 컴포넌트 간 상호작용 검증, 느림, 실제 환경과 유사
 *
 * 테스트 피라미드:
 *        /\        E2E (적게)
 *       /  \
 *      /----\      Integration (적당히)
 *     /      \
 *    /--------\    Unit (많이)
 */
@SpringBootTest
@Transactional  // 테스트 후 자동 롤백
@DisplayName("PaymentService 통합 테스트")
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        // @Transactional로 인해 자동 롤백되므로
        // 명시적 데이터 정리 불필요
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
            PaymentResult result = paymentService.processPayment(request);

            // Then - 결과 검증
            assertThat(result.originalPrice()).isEqualTo(10000);
            assertThat(result.discountedAmount()).isEqualTo(8500);  // 15% 할인
            assertThat(result.taxedAmount()).isEqualTo(9350);       // 10% 세금

            // Then - DB 저장 검증
            List<Payment> payments = paymentRepository.findAll();
            assertThat(payments)
                    .hasSize(1)
                    .first()
                    .satisfies(payment -> {
                        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
                        assertThat(payment.getTaxedAmount()).isEqualTo(9350);
                    });
        }

        @Test
        @DisplayName("VIP 고객은 15% 할인이 적용된다")
        void shouldApplyVipDiscount() {
            // Given
            PaymentRequest request = new PaymentRequest(10000, "KR", true);

            // When
            PaymentResult result = paymentService.processPayment(request);

            // Then
            assertThat(result.discountedAmount())
                    .as("VIP 할인 15%% 적용")
                    .isEqualTo(8500);
        }

        @Test
        @DisplayName("일반 고객은 10% 할인이 적용된다")
        void shouldApplyNormalDiscount() {
            // Given
            PaymentRequest request = new PaymentRequest(10000, "KR", false);

            // When
            PaymentResult result = paymentService.processPayment(request);

            // Then
            assertThat(result.discountedAmount())
                    .as("일반 할인 10%% 적용")
                    .isEqualTo(9000);
        }

        @Test
        @DisplayName("한국 결제는 10% 부가세가 적용된다")
        void shouldApplyKoreaTax() {
            // Given
            PaymentRequest request = new PaymentRequest(10000, "KR", true);

            // When
            PaymentResult result = paymentService.processPayment(request);

            // Then
            double expectedTax = 8500 * 1.1;  // 할인가 × 1.1
            assertThat(result.taxedAmount())
                    .as("한국 부가세 10%% 적용")
                    .isEqualTo(expectedTax);
        }
    }

    @Nested
    @DisplayName("결제 조회 통합 테스트")
    class GetPaymentTest {

        @Test
        @DisplayName("존재하는 결제를 ID로 조회할 수 있다")
        void shouldFindPaymentById() {
            // Given - 결제 생성
            PaymentRequest request = new PaymentRequest(10000, "KR", true);
            paymentService.processPayment(request);
            Payment savedPayment = paymentRepository.findAll().get(0);

            // When
            Payment found = paymentService.getPayment(savedPayment.getId());

            // Then
            assertThat(found.getId()).isEqualTo(savedPayment.getId());
            assertThat(found.getOriginalPrice()).isEqualTo(10000);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
        void shouldThrowExceptionForNonExistentId() {
            // Given
            Long nonExistentId = 9999L;

            // When & Then
            assertThatThrownBy(() -> paymentService.getPayment(nonExistentId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("결제를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("전체 결제 목록을 조회할 수 있다")
        void shouldFindAllPayments() {
            // Given
            paymentService.processPayment(new PaymentRequest(10000, "KR", true));
            paymentService.processPayment(new PaymentRequest(20000, "US", false));
            paymentService.processPayment(new PaymentRequest(30000, "KR", true));

            // When
            List<Payment> payments = paymentService.getAllPayments();

            // Then
            assertThat(payments).hasSize(3);
        }

        @Test
        @DisplayName("상태별로 결제를 조회할 수 있다")
        void shouldFindPaymentsByStatus() {
            // Given - 3건 결제 (모두 COMPLETED)
            paymentService.processPayment(new PaymentRequest(10000, "KR", true));
            paymentService.processPayment(new PaymentRequest(20000, "KR", false));
            paymentService.processPayment(new PaymentRequest(30000, "US", true));

            // When
            List<Payment> completedPayments = paymentService.getPaymentsByStatus(PaymentStatus.COMPLETED);
            List<Payment> pendingPayments = paymentService.getPaymentsByStatus(PaymentStatus.PENDING);

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
            paymentService.processPayment(new PaymentRequest(10000, "KR", true));
            Payment payment = paymentRepository.findAll().get(0);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

            // When
            Payment refunded = paymentService.refundPayment(payment.getId());

            // Then
            assertThat(refunded.getStatus()).isEqualTo(PaymentStatus.REFUNDED);

            // DB 검증
            Payment fromDb = paymentRepository.findById(payment.getId()).orElseThrow();
            assertThat(fromDb.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("존재하지 않는 결제는 환불할 수 없다")
        void shouldNotRefundNonExistentPayment() {
            // Given
            Long nonExistentId = 9999L;

            // When & Then
            assertThatThrownBy(() -> paymentService.refundPayment(nonExistentId))
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
            PaymentResult result = paymentService.processPayment(request);

            assertThat(result.originalPrice()).isEqualTo(50000);
            assertThat(result.discountedAmount()).isEqualTo(42500);  // 15% 할인
            assertThat(result.taxedAmount()).isEqualTo(46750);       // 10% 세금 (Math.round 적용)

            // 2. 결제 조회
            Payment payment = paymentRepository.findAll().get(0);
            Payment found = paymentService.getPayment(payment.getId());

            assertThat(found.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

            // 3. 환불
            Payment refunded = paymentService.refundPayment(payment.getId());

            assertThat(refunded.getStatus()).isEqualTo(PaymentStatus.REFUNDED);

            // 4. 상태별 조회로 검증
            List<Payment> completed = paymentService.getPaymentsByStatus(PaymentStatus.COMPLETED);
            List<Payment> refundedList = paymentService.getPaymentsByStatus(PaymentStatus.REFUNDED);

            assertThat(completed).isEmpty();
            assertThat(refundedList).hasSize(1);
        }
    }
}
