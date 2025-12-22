package com.example.payment_step2.unit.policy;

import com.example.payment_step2.domain.model.Money;
import com.example.payment_step2.policy.tax.KoreaVatPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * KoreaVatPolicy 단위 테스트
 *
 * [테스트 특징]
 * - Spring Context 없이 POJO 테스트
 * - Money.multiply()로 VAT 계산 검증
 */
class KoreaVatPolicyTest {

    private final KoreaVatPolicy policy = new KoreaVatPolicy();

    @Test
    @DisplayName("10% VAT 적용")
    void apply_10percentVat() {
        // given
        Money discountedPrice = Money.of(8500);

        // when
        // [변경] Money.multiply()로 VAT 계산
        Money taxedAmount = policy.apply(discountedPrice);

        // then
        // 8500 * 1.1 = 9350
        assertThat(taxedAmount.getAmount()).isEqualTo(9350);
    }

    @Test
    @DisplayName("Money 불변성 검증")
    void apply_moneyImmutability() {
        // given
        Money discountedPrice = Money.of(10000);

        // when
        Money taxedAmount = policy.apply(discountedPrice);

        // then
        // 원본 객체는 변경되지 않음
        assertThat(discountedPrice.getAmount()).isEqualTo(10000);
        assertThat(taxedAmount.getAmount()).isEqualTo(11000);
    }
}
