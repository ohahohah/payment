package com.example.payment_ddd_v1.application;

import com.example.payment_ddd_v1.domain.model.Country;
import com.example.payment_ddd_v1.domain.model.Money;
import com.example.payment_ddd_v1.domain.model.Payment;
import com.example.payment_ddd_v1.domain.model.PaymentStatus;
import com.example.payment_ddd_v1.domain.policy.CustomerDiscountPolicy;
import com.example.payment_ddd_v1.domain.policy.DiscountPolicy;
import com.example.payment_ddd_v1.domain.policy.KoreaTaxPolicy;
import com.example.payment_ddd_v1.domain.policy.TaxPolicy;
import com.example.payment_ddd_v1.domain.policy.VipDiscountPolicy;
import com.example.payment_ddd_v1.domain.repository.PaymentRepository;
import com.example.payment_ddd_v1.interfaces.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PaymentService 단위 테스트 (Mock 사용)
 *
 * [테스트 목적]
 * - Application Service의 유스케이스 흐름 검증
 * - Repository와의 협력 검증
 * - Policy 적용 검증
 *
 * [Mock 사용 이유]
 * - 단위 테스트: 외부 의존성 격리
 * - 빠른 테스트 실행
 * - DB 없이 테스트 가능
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 단위 테스트")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    private DiscountPolicy customerDiscountPolicy;
    private DiscountPolicy vipDiscountPolicy;
    private TaxPolicy taxPolicy;

    @BeforeEach
    void setUp() {
        customerDiscountPolicy = new CustomerDiscountPolicy();
        vipDiscountPolicy = new VipDiscountPolicy();
        taxPolicy = new KoreaTaxPolicy();

        paymentService = new PaymentService(
                paymentRepository,
                customerDiscountPolicy,
                vipDiscountPolicy,
                taxPolicy
        );
    }

    @Nested
    @DisplayName("결제 생성 테스트")
    class CreatePaymentTest {

        @Test
        @DisplayName("일반 고객 결제 생성 - 5% 할인 적용")
        void createPaymentForCustomer() {
            // given
            PaymentRequest request = new PaymentRequest();
            request.setPrice(10000.0);
            request.setCountryCode("KR");
            request.setIsVip(false);

            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Payment result = paymentService.createPayment(request);

            // then
            assertThat(result.getOriginalPrice().getAmount()).isEqualTo(10000);
            assertThat(result.getDiscountedAmount().getAmount()).isEqualTo(9500); // 5% 할인
            assertThat(result.getTaxedAmount().getAmount()).isEqualTo(10450);     // 10% 세금
            assertThat(result.isVip()).isFalse();
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);

            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("VIP 고객 결제 생성 - 10% 할인 적용")
        void createPaymentForVip() {
            // given
            PaymentRequest request = new PaymentRequest();
            request.setPrice(10000.0);
            request.setCountryCode("KR");
            request.setIsVip(true);

            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Payment result = paymentService.createPayment(request);

            // then
            assertThat(result.getOriginalPrice().getAmount()).isEqualTo(10000);
            assertThat(result.getDiscountedAmount().getAmount()).isEqualTo(9000); // 10% 할인
            assertThat(result.getTaxedAmount().getAmount()).isEqualTo(9900);      // 10% 세금
            assertThat(result.isVip()).isTrue();

            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("결제 조회 테스트")
    class GetPaymentTest {

        @Test
        @DisplayName("존재하는 결제 조회")
        void getExistingPayment() {
            // given
            Payment payment = Payment.create(
                    Money.of(10000),
                    Money.of(9000),
                    Money.of(9900),
                    Country.of("KR"),
                    false
            );
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            // when
            Payment result = paymentService.getPayment(1L);

            // then
            assertThat(result).isEqualTo(payment);
            verify(paymentRepository).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 결제 조회 시 예외")
        void getNotExistingPayment() {
            // given
            when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.getPayment(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("결제를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("결제 완료 테스트")
    class CompletePaymentTest {

        @Test
        @DisplayName("결제 완료 처리")
        void completePayment() {
            // given
            Payment payment = Payment.create(
                    Money.of(10000),
                    Money.of(9000),
                    Money.of(9900),
                    Country.of("KR"),
                    false
            );
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            // when
            Payment result = paymentService.completePayment(1L);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("환불 테스트")
    class RefundPaymentTest {

        @Test
        @DisplayName("완료된 결제 환불")
        void refundCompletedPayment() {
            // given
            Payment payment = Payment.create(
                    Money.of(10000),
                    Money.of(9000),
                    Money.of(9900),
                    Country.of("KR"),
                    false
            );
            payment.complete();
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            // when
            Payment result = paymentService.refundPayment(1L);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("대기 상태 결제 환불 시도 시 예외")
        void cannotRefundPendingPayment() {
            // given
            Payment payment = Payment.create(
                    Money.of(10000),
                    Money.of(9000),
                    Money.of(9900),
                    Country.of("KR"),
                    false
            );
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            // when & then
            assertThatThrownBy(() -> paymentService.refundPayment(1L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("결제 실패 테스트")
    class FailPaymentTest {

        @Test
        @DisplayName("결제 실패 처리")
        void failPayment() {
            // given
            Payment payment = Payment.create(
                    Money.of(10000),
                    Money.of(9000),
                    Money.of(9900),
                    Country.of("KR"),
                    false
            );
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            // when
            Payment result = paymentService.failPayment(1L);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }
}
