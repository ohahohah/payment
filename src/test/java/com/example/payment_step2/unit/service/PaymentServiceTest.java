package com.example.payment_step2.unit.service;

import com.example.payment_step2.domain.model.Money;
import com.example.payment_step2.dto.PaymentRequest;
import com.example.payment_step2.dto.PaymentResult;
import com.example.payment_step2.entity.Payment;
import com.example.payment_step2.handler.PaymentCompletionHandler;
import com.example.payment_step2.policy.discount.CustomerDiscountPolicy;
import com.example.payment_step2.policy.tax.TaxPolicy;
import com.example.payment_step2.repository.PaymentRepository;
import com.example.payment_step2.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

/**
 * PaymentService 단위 테스트
 *
 * [테스트 범위]
 * - Service 계층의 비즈니스 로직
 * - Value Object 변환 검증
 * - Policy 호출 흐름 검증
 *
 * [Mockito 사용]
 * - Repository, Policy, Handler를 Mock으로 대체
 * - Spring Context 없이 빠른 테스트
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CustomerDiscountPolicy discountPolicy;

    @Mock
    private TaxPolicy taxPolicy;

    @Mock
    private PaymentCompletionHandler completionHandler;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepository,
                discountPolicy,
                taxPolicy,
                List.of(completionHandler)
        );
    }

    @Test
    @DisplayName("결제 처리 성공 - Money와 Country Value Object 사용")
    void processPayment_success() {
        // given
        PaymentRequest request = new PaymentRequest(10000, "KR", true);

        Money originalPrice = Money.of(10000);
        Money discountedAmount = Money.of(8500);
        Money taxedAmount = Money.of(9350);

        when(discountPolicy.apply(any(Money.class), anyBoolean()))
                .thenReturn(discountedAmount);
        when(taxPolicy.apply(any(Money.class)))
                .thenReturn(taxedAmount);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        PaymentResult result = paymentService.processPayment(request);

        // then
        assertThat(result.originalPrice()).isEqualTo(10000);
        assertThat(result.discountedAmount()).isEqualTo(8500);
        assertThat(result.taxedAmount()).isEqualTo(9350);
        assertThat(result.country()).isEqualTo("KR");
        assertThat(result.isVip()).isTrue();
    }

    @Test
    @DisplayName("음수 금액 요청 시 Money.of()에서 예외 발생")
    void processPayment_negativeAmount_throwsException() {
        // given
        PaymentRequest request = new PaymentRequest(-1000, "KR", true);

        // when & then
        // [변경] Service의 if문이 아닌 Money.of()에서 검증
        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("0 이상");
    }

    @Test
    @DisplayName("지원하지 않는 국가 요청 시 Country.of()에서 예외 발생")
    void processPayment_unsupportedCountry_throwsException() {
        // given
        PaymentRequest request = new PaymentRequest(10000, "JP", true);

        // when & then
        // [변경] Country.of()에서 유효성 검증
        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 국가");
    }
}
