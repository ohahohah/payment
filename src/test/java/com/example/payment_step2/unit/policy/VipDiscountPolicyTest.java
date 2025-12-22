package com.example.payment_step2.unit.policy;

import com.example.payment_step2.domain.model.Money;
import com.example.payment_step2.policy.discount.VipDiscountPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * VipDiscountPolicy 단위 테스트
 *
 * [테스트 특징]
 * - Spring Context 없이 POJO 테스트
 * - Money Value Object 사용으로 타입 안전성 검증
 */
class VipDiscountPolicyTest {

    private final VipDiscountPolicy policy = new VipDiscountPolicy();

    @Test
    @DisplayName("VIP 고객은 15% 할인 적용")
    void apply_vipCustomer_15percentDiscount() {
        // given
        Money originalPrice = Money.of(10000);
        boolean isVip = true;

        // when
        // [변경] Money.multiply()로 할인 계산
        Money discounted = policy.apply(originalPrice, isVip);

        // then
        assertThat(discounted.getAmount()).isEqualTo(8500);
    }

    @Test
    @DisplayName("일반 고객은 할인 없음")
    void apply_normalCustomer_noDiscount() {
        // given
        Money originalPrice = Money.of(10000);
        boolean isVip = false;

        // when
        Money discounted = policy.apply(originalPrice, isVip);

        // then
        assertThat(discounted.getAmount()).isEqualTo(10000);
    }

    @Test
    @DisplayName("Money 불변성 검증 - 원본 객체 변경되지 않음")
    void apply_moneyImmutability() {
        // given
        Money originalPrice = Money.of(10000);

        // when
        Money discounted = policy.apply(originalPrice, true);

        // then
        // 원본 객체는 변경되지 않음 (불변성)
        assertThat(originalPrice.getAmount()).isEqualTo(10000);
        assertThat(discounted.getAmount()).isEqualTo(8500);
    }
}
