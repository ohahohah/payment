package com.example.payment_ddd.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CountryTest - Country Value Object 단위 테스트
 */
@DisplayName("Country Value Object 테스트")
class CountryTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("한국 코드로 생성")
        void createKorea() {
            Country korea = Country.of("KR");
            assertThat(korea.isKorea()).isTrue();
            assertThat(korea.getCode()).isEqualTo("KR");
        }

        @Test
        @DisplayName("미국 코드로 생성")
        void createUs() {
            Country us = Country.of("US");
            assertThat(us.isUs()).isTrue();
            assertThat(us.getCode()).isEqualTo("US");
        }

        @Test
        @DisplayName("소문자도 대문자로 정규화")
        void normalizeToUpperCase() {
            Country korea = Country.of("kr");
            assertThat(korea.getCode()).isEqualTo("KR");
        }

        @Test
        @DisplayName("지원하지 않는 국가는 거부")
        void rejectUnsupportedCountry() {
            assertThatThrownBy(() -> Country.of("JP"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("지원하지 않는 국가");
        }

        @Test
        @DisplayName("null 코드는 거부")
        void rejectNullCode() {
            assertThatThrownBy(() -> Country.of(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("빈 코드는 거부")
        void rejectBlankCode() {
            assertThatThrownBy(() -> Country.of(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("팩토리 메서드 테스트")
    class FactoryMethodTest {

        @Test
        @DisplayName("korea() 메서드")
        void koreaFactory() {
            Country korea = Country.korea();
            assertThat(korea.isKorea()).isTrue();
        }

        @Test
        @DisplayName("us() 메서드")
        void usFactory() {
            Country us = Country.us();
            assertThat(us.isUs()).isTrue();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 국가 코드는 동등")
        void equalsByValue() {
            Country korea1 = Country.of("KR");
            Country korea2 = Country.korea();

            assertThat(korea1).isEqualTo(korea2);
            assertThat(korea1.hashCode()).isEqualTo(korea2.hashCode());
        }
    }
}
