package com.example.payment_step2_2.unit.service;

import com.example.payment_step2_2.domain.model.Country;
import com.example.payment_step2_2.domain.model.Money;
import com.example.payment_step2_2.domain.policy.DiscountPolicy;
import com.example.payment_step2_2.domain.policy.TaxPolicy;
import com.example.payment_step2_2.dto.PaymentRequest;
import com.example.payment_step2_2.entity.Payment;
import com.example.payment_step2_2.entity.PaymentStatus;
import com.example.payment_step2_2.repository.PaymentRepository;
import com.example.payment_step2_2.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * PaymentServiceTest - Service 단위 테스트
 *
 * ============================================================================
 * [payment_step1과의 차이점]
 * ============================================================================
 *
 * payment_step1: Service가 상태 검증 로직을 가지므로 테스트가 복잡
 *   - 상태 검증 로직 테스트
 *   - setter 호출 여부 테스트
 *
 * payment_step2_2: Service는 위임만 하므로 테스트가 단순
 *   - Entity의 비즈니스 메서드 호출 여부만 확인
 *   - 상태 검증 로직은 Entity 테스트에서 검증
 *
 * ============================================================================
 * [테스트 전략]
 * ============================================================================
 *
 * Rich Domain Model에서는:
 * - Entity 테스트: 핵심 비즈니스 로직 (상태 전이 규칙) 검증
 * - Service 테스트: 위임(delegation) 확인 (Entity 메서드 호출 여부)
 *
 * 이렇게 분리하면:
 * 1. 테스트가 더 집중적이고 명확해짐
 * 2. 비즈니스 로직 변경 시 Entity 테스트만 수정
 * 3. Service 변경 시 Service 테스트만 수정
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 테스트")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private DiscountPolicy customerDiscountPolicy;
    @Mock
    private DiscountPolicy vipDiscountPolicy;
    @Mock
    private TaxPolicy taxPolicy;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepository,
                customerDiscountPolicy,
                vipDiscountPolicy,
                taxPolicy);
    }

    @Nested
    @DisplayName("createPayment()")
    class CreatePaymentTest {

        @Test
        @DisplayName("VIP 고객 결제 생성 시 VIP 할인 정책 적용")
        void shouldApplyVipDiscount() {
            // Given
            PaymentRequest request = new PaymentRequest(10000.0, "KR", true);
            given(vipDiscountPolicy.calculateDiscount(any())).willReturn(Money.of(1000.0));
            given(taxPolicy.applyTax(any())).willReturn(Money.of(9900.0));
            given(paymentRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // When
            Payment result = paymentService.createPayment(request);

            // Then
            assertEquals(PaymentStatus.PENDING, result.getStatus());
            assertTrue(result.getIsVip());
            verify(vipDiscountPolicy).calculateDiscount(any());
        }

        @Test
        @DisplayName("일반 고객 결제 생성 시 일반 할인 정책 적용")
        void shouldApplyCustomerDiscount() {
            // Given
            PaymentRequest request = new PaymentRequest(10000.0, "KR", false);
            given(customerDiscountPolicy.calculateDiscount(any())).willReturn(Money.of(500.0));
            given(taxPolicy.applyTax(any())).willReturn(Money.of(10450.0));
            given(paymentRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // When
            Payment result = paymentService.createPayment(request);

            // Then
            assertEquals(PaymentStatus.PENDING, result.getStatus());
            assertFalse(result.getIsVip());
            verify(customerDiscountPolicy).calculateDiscount(any());
        }
    }

    @Nested
    @DisplayName("completePayment()")
    class CompletePaymentTest {

        @Test
        @DisplayName("결제 완료 처리 - Entity의 complete() 메서드 호출")
        void shouldDelegateToEntityComplete() {
            // Given
            Payment payment = Payment.create(
                    Money.of(10000.0), Money.of(9000.0), Money.of(9900.0),
                    Country.of("KR"), true);
            given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

            // When
            Payment result = paymentService.completePayment(1L);

            // Then
            // [핵심] Service는 Entity의 complete() 메서드에 위임
            // 상태 검증 로직은 Entity 내부에서 처리됨
            assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        }
    }

    @Nested
    @DisplayName("refundPayment()")
    class RefundPaymentTest {

        @Test
        @DisplayName("환불 처리 - Entity의 refund() 메서드 호출")
        void shouldDelegateToEntityRefund() {
            // Given
            Payment payment = Payment.create(
                    Money.of(10000.0), Money.of(9000.0), Money.of(9900.0),
                    Country.of("KR"), true);
            payment.complete();  // COMPLETED 상태로 변경
            given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

            // When
            Payment result = paymentService.refundPayment(1L);

            // Then
            // [핵심] Service는 Entity의 refund() 메서드에 위임
            // 상태 검증(COMPLETED 상태인지)은 Entity 내부에서 처리됨
            assertEquals(PaymentStatus.REFUNDED, result.getStatus());
        }

        @Test
        @DisplayName("PENDING 상태에서 환불 시도 - Entity에서 예외 발생")
        void shouldThrowExceptionFromEntityWhenPending() {
            // Given
            Payment payment = Payment.create(
                    Money.of(10000.0), Money.of(9000.0), Money.of(9900.0),
                    Country.of("KR"), true);
            // PENDING 상태 유지
            given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

            // When & Then
            // [핵심] 예외는 Entity의 refund() 메서드에서 발생
            // Service는 검증 로직을 가지지 않음
            assertThrows(IllegalStateException.class,
                    () -> paymentService.refundPayment(1L));
        }
    }

    @Nested
    @DisplayName("failPayment()")
    class FailPaymentTest {

        @Test
        @DisplayName("결제 실패 처리 - Entity의 fail() 메서드 호출")
        void shouldDelegateToEntityFail() {
            // Given
            Payment payment = Payment.create(
                    Money.of(10000.0), Money.of(9000.0), Money.of(9900.0),
                    Country.of("KR"), true);
            given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

            // When
            Payment result = paymentService.failPayment(1L);

            // Then
            assertEquals(PaymentStatus.FAILED, result.getStatus());
        }
    }
}
