package com.example.payment_step2.integration;

import com.example.payment_step2.dto.PaymentRequest;
import com.example.payment_step2.dto.PaymentResult;
import com.example.payment_step2.entity.Payment;
import com.example.payment_step2.entity.PaymentStatus;
import com.example.payment_step2.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PaymentStep1 통합 테스트
 *
 * [@SpringBootTest 사용]
 * - 전체 Spring Context 로드
 * - 실제 컴포넌트 간 연동 테스트
 *
 * [테스트 범위]
 * - Service -> Repository -> DB 전체 흐름
 * - @Convert를 통한 Value Object 영속화
 * - 비즈니스 메서드를 통한 상태 전이
 *
 * [주의]
 * - 로직 검증은 단위 테스트에서 수행
 * - 여기서는 연동만 확인
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentStep1IntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 생성 및 조회 통합 테스트")
    void createAndGetPayment() {
        // given
        PaymentRequest request = new PaymentRequest(10000, "KR", true);

        // when
        PaymentResult result = paymentService.processPayment(request);

        // then - 연동 성공 확인 (로직 검증은 단위 테스트에서)
        assertThat(result).isNotNull();
        assertThat(result.country()).isEqualTo("KR");
    }

    @Test
    @DisplayName("결제 생성 후 환불 통합 테스트")
    void createAndRefundPayment() {
        // given
        PaymentRequest request = new PaymentRequest(10000, "KR", true);
        paymentService.processPayment(request);

        // 생성된 결제 조회 (첫 번째 결제)
        Payment payment = paymentService.getAllPayments().get(0);

        // when
        Payment refunded = paymentService.refundPayment(payment.getId());

        // then - 상태 변경 확인
        assertThat(refunded.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }
}
