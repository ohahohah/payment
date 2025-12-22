package com.example.payment_step2.unit.entity;

import com.example.payment_step2.domain.model.Country;
import com.example.payment_step2.domain.model.Money;
import com.example.payment_step2.entity.Payment;
import com.example.payment_step2.entity.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Payment 엔티티 단위 테스트
 *
 * [테스트 범위]
 * - Value Object 사용 검증 (Money, Country)
 * - @Convert를 통한 타입 변환 검증은 Repository 테스트에서
 *
 * [payment_step1의 변경 범위]
 * - Value Object만 적용 (Rich Domain Model은 미적용)
 * - setter를 통한 상태 변경 (payment_ul과 동일)
 *
 * [Spring Context 없음]
 * - POJO 테스트로 빠른 실행
 */
class PaymentTest {

    @Test
    @DisplayName("결제 생성 시 PENDING 상태")
    void create_initialStatusIsPending() {
        // given & when
        Payment payment = Payment.create(
                Money.of(10000),
                Money.of(8500),
                Money.of(9350),
                Country.korea(),
                true
        );

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Value Object 필드 검증 - Money 타입 사용")
    void valueObjectFields_money() {
        // given
        Money originalPrice = Money.of(10000);
        Money discountedAmount = Money.of(8500);
        Money taxedAmount = Money.of(9350);

        // when
        Payment payment = Payment.create(
                originalPrice, discountedAmount, taxedAmount,
                Country.korea(), true);

        // then - Value Object가 제대로 저장되었는지 확인
        assertThat(payment.getOriginalPrice()).isEqualTo(originalPrice);
        assertThat(payment.getDiscountedAmount()).isEqualTo(discountedAmount);
        assertThat(payment.getTaxedAmount()).isEqualTo(taxedAmount);
    }

    @Test
    @DisplayName("Value Object 필드 검증 - Country 타입 사용")
    void valueObjectFields_country() {
        // given
        Country country = Country.korea();

        // when
        Payment payment = Payment.create(
                Money.of(10000), Money.of(8500), Money.of(9350),
                country, true);

        // then
        assertThat(payment.getCountry()).isEqualTo(country);
        assertThat(payment.getCountry().isKorea()).isTrue();
    }

    @Test
    @DisplayName("setter로 상태 변경 - payment_ul과 동일")
    void setStatus_changesStatus() {
        // given
        Payment payment = createSamplePayment();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        // when
        payment.setStatus(PaymentStatus.COMPLETED);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("Money 값 동등성 검증")
    void money_equality() {
        // given
        Payment payment1 = Payment.create(
                Money.of(10000), Money.of(8500), Money.of(9350),
                Country.korea(), true);
        Payment payment2 = Payment.create(
                Money.of(10000), Money.of(8500), Money.of(9350),
                Country.korea(), true);

        // then - 같은 값의 Money는 equals가 true
        assertThat(payment1.getOriginalPrice())
                .isEqualTo(payment2.getOriginalPrice());
    }

    private Payment createSamplePayment() {
        return Payment.create(
                Money.of(10000),
                Money.of(8500),
                Money.of(9350),
                Country.korea(),
                true
        );
    }
}
