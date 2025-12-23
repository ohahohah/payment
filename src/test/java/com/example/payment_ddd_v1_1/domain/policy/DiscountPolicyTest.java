package com.example.payment_ddd_v1_1.domain.policy;

import com.example.payment_ddd_v1_1.domain.model.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * DiscountPolicy 단위 테스트 (순수 Java)
 *
 * [테스트 대상]
 * - CustomerDiscountPolicy: 일반 고객 할인 (5%)
 * - VipDiscountPolicy: VIP 고객 할인 (10%)
 */
@DisplayName("DiscountPolicy 테스트")
class DiscountPolicyTest {

    @Nested
    @DisplayName("CustomerDiscountPolicy 테스트")
    class CustomerDiscountPolicyTest {

        private final DiscountPolicy policy = new CustomerDiscountPolicy();

        @Test
        @DisplayName("일반 고객은 5% 할인")
        void calculateDiscount() {
            // given
            Money originalPrice = Money.of(10000);

            // when
            Money discount = policy.calculateDiscount(originalPrice);

            // then
            assertThat(discount.getAmount()).isEqualTo(500); // 5%
        }

        @Test
        @DisplayName("0원에 대한 할인")
        void calculateDiscountForZero() {
            // given
            Money originalPrice = Money.zero();

            // when
            Money discount = policy.calculateDiscount(originalPrice);

            // then
            assertThat(discount.getAmount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("VipDiscountPolicy 테스트")
    class VipDiscountPolicyTest {

        private final DiscountPolicy policy = new VipDiscountPolicy();

        @Test
        @DisplayName("VIP는 10% 할인")
        void calculateDiscount() {
            // given
            Money originalPrice = Money.of(10000);

            // when
            Money discount = policy.calculateDiscount(originalPrice);

            // then
            assertThat(discount.getAmount()).isEqualTo(1000); // 10%
        }
    }

    @Nested
    @DisplayName("TaxPolicy 테스트")
    class TaxPolicyTest {

        private final TaxPolicy policy = new KoreaTaxPolicy();

        @Test
        @DisplayName("한국 세금 10% 적용")
        void applyTax() {
            // given
            Money discountedAmount = Money.of(9000);

            // when
            Money taxedAmount = policy.applyTax(discountedAmount);

            // then
            assertThat(taxedAmount.getAmount()).isEqualTo(9900); // 10% 세금
        }
    }

    @Nested
    @DisplayName("정책 비교 테스트")
    class PolicyComparisonTest {

        @Test
        @DisplayName("VIP 할인이 일반 고객보다 큼")
        void vipDiscountIsGreaterThanCustomer() {
            // given
            Money originalPrice = Money.of(10000);
            DiscountPolicy customerPolicy = new CustomerDiscountPolicy();
            DiscountPolicy vipPolicy = new VipDiscountPolicy();

            // when
            Money customerDiscount = customerPolicy.calculateDiscount(originalPrice);
            Money vipDiscount = vipPolicy.calculateDiscount(originalPrice);

            // then
            assertThat(vipDiscount.isGreaterThan(customerDiscount)).isTrue();
        }
    }
}
