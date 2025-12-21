package com.example.payment_ddd.domain.service;

import com.example.payment_ddd.domain.model.Country;
import com.example.payment_ddd.domain.model.Money;
import com.example.payment_ddd.domain.model.Payment;
import com.example.payment_ddd.domain.model.PaymentStatus;
import com.example.payment_ddd.domain.policy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PaymentDomainServiceTest - 도메인 서비스 단위 테스트
 *
 * [도메인 서비스 테스트 특징]
 * - 순수 Java 테스트 (Spring 불필요)
 * - 인프라 의존성 없음
 * - 빠른 실행
 */
@DisplayName("PaymentDomainService 테스트")
class PaymentDomainServiceTest {

    private PaymentDomainService paymentDomainService;

    @BeforeEach
    void setUp() {
        DiscountPolicy discountPolicy = new VipDiscountPolicy();
        List<TaxPolicy> taxPolicies = List.of(
                new KoreaTaxPolicy(),
                new UsTaxPolicy()
        );
        paymentDomainService = new PaymentDomainService(discountPolicy, taxPolicies);
    }

    @Nested
    @DisplayName("결제 생성")
    class CreatePaymentTest {

        @Test
        @DisplayName("한국 VIP 결제 생성")
        void createKoreanVipPayment() {
            Money originalPrice = Money.of(10000);

            Payment payment = paymentDomainService.createPayment(originalPrice, Country.korea(), true);

            // 10000 * 0.9 (VIP 할인) * 1.1 (VAT) = 9900
            assertThat(payment.getOriginalPrice().getAmount()).isEqualTo(10000);
            assertThat(payment.getDiscountedAmount().getAmount()).isEqualTo(9000);
            assertThat(payment.getTaxedAmount().getAmount()).isEqualTo(9900);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("한국 일반 고객 결제 생성")
        void createKoreanNonVipPayment() {
            Money originalPrice = Money.of(10000);

            Payment payment = paymentDomainService.createPayment(originalPrice, Country.korea(), false);

            // 10000 * 1.0 * 1.1 = 11000
            assertThat(payment.getDiscountedAmount().getAmount()).isEqualTo(10000);
            assertThat(payment.getTaxedAmount().getAmount()).isEqualTo(11000);
        }

        @Test
        @DisplayName("미국 VIP 결제 생성")
        void createUsVipPayment() {
            Money originalPrice = Money.of(10000);

            Payment payment = paymentDomainService.createPayment(originalPrice, Country.us(), true);

            // 10000 * 0.9 (VIP 할인) * 1.08 (Sales Tax) = 9720
            assertThat(payment.getDiscountedAmount().getAmount()).isEqualTo(9000);
            assertThat(payment.getTaxedAmount().getAmount()).isEqualTo(9720);
        }

        @Test
        @DisplayName("미국 일반 고객 결제 생성")
        void createUsNonVipPayment() {
            Money originalPrice = Money.of(10000);

            Payment payment = paymentDomainService.createPayment(originalPrice, Country.us(), false);

            // 10000 * 1.0 * 1.08 = 10800
            assertThat(payment.getDiscountedAmount().getAmount()).isEqualTo(10000);
            assertThat(payment.getTaxedAmount().getAmount()).isEqualTo(10800);
        }
    }
}
