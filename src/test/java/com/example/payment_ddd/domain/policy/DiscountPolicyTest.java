package com.example.payment_ddd.domain.policy;

import com.example.payment_ddd.domain.model.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * DiscountPolicyTest - 할인 정책 단위 테스트
 */
@DisplayName("할인 정책 테스트")
class DiscountPolicyTest {

    private final DiscountPolicy discountPolicy = new VipDiscountPolicy();

    @Nested
    @DisplayName("VIP 할인 정책")
    class VipDiscountPolicyTest {

        @Test
        @DisplayName("VIP는 10% 할인")
        void vipGets10PercentDiscount() {
            Money originalPrice = Money.of(10000);

            Money discountedPrice = discountPolicy.applyDiscount(originalPrice, true);

            assertThat(discountedPrice.getAmount()).isEqualTo(9000);
        }

        @Test
        @DisplayName("일반 고객은 할인 없음")
        void nonVipNoDiscount() {
            Money originalPrice = Money.of(10000);

            Money discountedPrice = discountPolicy.applyDiscount(originalPrice, false);

            assertThat(discountedPrice.getAmount()).isEqualTo(10000);
        }

        @Test
        @DisplayName("할인 금액 반올림 처리")
        void discountRounding() {
            Money originalPrice = Money.of(10001);

            Money discountedPrice = discountPolicy.applyDiscount(originalPrice, true);

            // 10001 * 0.9 = 9000.9 → 반올림 → 9001
            assertThat(discountedPrice.getAmount()).isEqualTo(9001);
        }
    }
}
