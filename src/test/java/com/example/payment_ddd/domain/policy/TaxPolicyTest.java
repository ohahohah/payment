package com.example.payment_ddd.domain.policy;

import com.example.payment_ddd.domain.model.Country;
import com.example.payment_ddd.domain.model.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * TaxPolicyTest - 세금 정책 단위 테스트
 */
@DisplayName("세금 정책 테스트")
class TaxPolicyTest {

    @Nested
    @DisplayName("한국 세금 정책")
    class KoreaTaxPolicyTest {

        private final TaxPolicy taxPolicy = new KoreaTaxPolicy();

        @Test
        @DisplayName("한국 지원")
        void supportsKorea() {
            assertThat(taxPolicy.supports(Country.korea())).isTrue();
        }

        @Test
        @DisplayName("미국 미지원")
        void doesNotSupportUs() {
            assertThat(taxPolicy.supports(Country.us())).isFalse();
        }

        @Test
        @DisplayName("VAT 10% 적용")
        void apply10PercentVat() {
            Money amount = Money.of(10000);

            Money taxedAmount = taxPolicy.applyTax(amount);

            assertThat(taxedAmount.getAmount()).isEqualTo(11000);
        }
    }

    @Nested
    @DisplayName("미국 세금 정책")
    class UsTaxPolicyTest {

        private final TaxPolicy taxPolicy = new UsTaxPolicy();

        @Test
        @DisplayName("미국 지원")
        void supportsUs() {
            assertThat(taxPolicy.supports(Country.us())).isTrue();
        }

        @Test
        @DisplayName("한국 미지원")
        void doesNotSupportKorea() {
            assertThat(taxPolicy.supports(Country.korea())).isFalse();
        }

        @Test
        @DisplayName("Sales Tax 8% 적용")
        void apply8PercentSalesTax() {
            Money amount = Money.of(10000);

            Money taxedAmount = taxPolicy.applyTax(amount);

            assertThat(taxedAmount.getAmount()).isEqualTo(10800);
        }
    }
}
