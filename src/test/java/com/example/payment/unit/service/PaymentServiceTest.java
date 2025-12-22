package com.example.payment.unit.service;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResult;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.observer.PaymentObserver;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.service.PaymentService;
import com.example.payment.strategy.discount.DiscountStrategy;
import com.example.payment.strategy.tax.TaxStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * PaymentService 단위 테스트
 *
 * - Spring Context 없이 순수 Mockito로 테스트
 * - 모든 의존성을 Mock으로 대체
 * - Service 로직만 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 단위 테스트")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private DiscountStrategy discountStrategy;

    @Mock
    private TaxStrategy taxStrategy;

    @Mock
    private PaymentObserver observer;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepository,
                discountStrategy,
                taxStrategy,
                List.of(observer)
        );
    }

    @Nested
    @DisplayName("결제 처리 테스트")
    class ExecuteTest {

        @Test
        @DisplayName("결제 요청 시 할인과 세금이 순서대로 적용된다")
        void shouldApplyDiscountAndTaxInOrder() {
            // Given
            PaymentRequest request = new PaymentRequest(10000, "KR", true);

            given(discountStrategy.apply(10000, true)).willReturn(8500.0);
            given(taxStrategy.apply(8500.0)).willReturn(9350.0);
            given(paymentRepository.save(any(Payment.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            // When
            PaymentResult result = paymentService.execute(request);

            // Then
            assertThat(result).isNotNull();
            then(discountStrategy).should().apply(10000, true);
            then(taxStrategy).should().apply(8500.0);
        }

        @Test
        @DisplayName("결제 완료 후 저장소에 저장된다")
        void shouldSavePaymentToRepository() {
            // Given
            PaymentRequest request = new PaymentRequest(10000, "KR", true);

            given(discountStrategy.apply(anyDouble(), anyBoolean())).willReturn(8500.0);
            given(taxStrategy.apply(anyDouble())).willReturn(9350.0);
            given(paymentRepository.save(any(Payment.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            // When
            paymentService.execute(request);

            // Then
            ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
            then(paymentRepository).should().save(captor.capture());

            Payment saved = captor.getValue();
            assertThat(saved.getAmt1()).isEqualTo(10000);
        }

        @Test
        @DisplayName("결제 완료 후 옵저버에게 알림이 전달된다")
        void shouldNotifyObservers() {
            // Given
            PaymentRequest request = new PaymentRequest(10000, "KR", true);

            given(discountStrategy.apply(anyDouble(), anyBoolean())).willReturn(8500.0);
            given(taxStrategy.apply(anyDouble())).willReturn(9350.0);
            given(paymentRepository.save(any(Payment.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            // When
            paymentService.execute(request);

            // Then
            then(observer).should(times(1)).onPaymentCompleted(any(PaymentResult.class));
        }

        @Test
        @DisplayName("음수 금액 요청 시 예외가 발생한다")
        void shouldThrowExceptionForNegativeAmount() {
            // Given
            PaymentRequest request = new PaymentRequest(-1000, "KR", true);

            // When & Then
            assertThatThrownBy(() -> paymentService.execute(request))
                    .isInstanceOf(IllegalArgumentException.class);

            then(paymentRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("결제 조회 테스트")
    class GetDataTest {

        @Test
        @DisplayName("존재하는 ID로 조회하면 결제를 반환한다")
        void shouldReturnPaymentWhenExists() {
            // Given
            Payment payment = Payment.create(10000.0, 8500.0, 9350.0, "KR", true);
            given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

            // When
            Payment found = paymentService.getData(1L);

            // Then
            assertThat(found).isNotNull();
            assertThat(found.getAmt1()).isEqualTo(10000);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
        void shouldThrowExceptionWhenNotExists() {
            // Given
            given(paymentRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentService.getData(999L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("결제 목록 조회 테스트")
    class GetListTest {

        @Test
        @DisplayName("전체 목록을 조회한다")
        void shouldReturnAllPayments() {
            // Given
            List<Payment> payments = List.of(
                    Payment.create(10000.0, 8500.0, 9350.0, "KR", true),
                    Payment.create(20000.0, 17000.0, 18700.0, "KR", true)
            );
            given(paymentRepository.findAll()).willReturn(payments);

            // When
            List<Payment> result = paymentService.getList();

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("상태별로 조회한다")
        void shouldReturnPaymentsByStatus() {
            // Given
            Payment completed = Payment.create(10000.0, 8500.0, 9350.0, "KR", true);
            completed.setStat(PaymentStatus.C);
            given(paymentRepository.findByStat(PaymentStatus.C))
                    .willReturn(List.of(completed));

            // When
            List<Payment> result = paymentService.getListByStat(PaymentStatus.C);

            // Then
            assertThat(result).hasSize(1);
            then(paymentRepository).should().findByStat(PaymentStatus.C);
        }
    }

    @Nested
    @DisplayName("환불 테스트")
    class RefundTest {

        @Test
        @DisplayName("완료 상태의 결제를 환불할 수 있다")
        void shouldRefundCompletedPayment() {
            // Given
            Payment payment = Payment.create(10000.0, 8500.0, 9350.0, "KR", true);
            payment.setStat(PaymentStatus.C);
            given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

            // When
            Payment refunded = paymentService.updateStatus(1L);

            // Then
            assertThat(refunded.getStat()).isEqualTo(PaymentStatus.R);
        }

        @Test
        @DisplayName("완료 상태가 아닌 결제는 환불할 수 없다")
        void shouldNotRefundNonCompletedPayment() {
            // Given
            Payment payment = Payment.create(10000.0, 8500.0, 9350.0, "KR", true);
            payment.setStat(PaymentStatus.P);  // 대기 상태
            given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

            // When & Then
            assertThatThrownBy(() -> paymentService.updateStatus(1L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
