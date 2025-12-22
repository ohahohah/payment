package com.example.payment_step2.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CountryTest - Country Value Object 단위 테스트
 *
 * [테스트 포인트]
 * 1. 자가 검증 - 지원 국가만 허용
 * 2. 정규화 - 소문자 → 대문자
 * 3. 비즈니스 메서드 - isKorea(), isUs()
 * 4. 동등성 - 같은 코드면 같은 객체
 */
@DisplayName("Country Value Object 테스트")
class CountryTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("한국 코드로 생성")
        void createKorea() {
            // When
            Country korea = Country.of("KR");

            // Then
            assertThat(korea.getCode()).isEqualTo("KR");
            assertThat(korea.isKorea()).isTrue();
            assertThat(korea.isUs()).isFalse();
        }

        @Test
        @DisplayName("미국 코드로 생성")
        void createUs() {
            // When
            Country us = Country.of("US");

            // Then
            assertThat(us.getCode()).isEqualTo("US");
            assertThat(us.isUs()).isTrue();
            assertThat(us.isKorea()).isFalse();
        }

        @Test
        @DisplayName("소문자도 대문자로 정규화")
        void normalizeToUpperCase() {
            // When
            Country korea = Country.of("kr");

            // Then
            assertThat(korea.getCode()).isEqualTo("KR");
            assertThat(korea.isKorea()).isTrue();
        }

        @Test
        @DisplayName("지원하지 않는 국가는 거부 - 자가 검증")
        void rejectUnsupportedCountry() {
            // When & Then
            assertThatThrownBy(() -> Country.of("JP"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("지원하지 않는 국가");
        }

        @Test
        @DisplayName("null 코드는 거부")
        void rejectNullCode() {
            // When & Then
            assertThatThrownBy(() -> Country.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("필수");
        }

        @Test
        @DisplayName("빈 코드는 거부")
        void rejectBlankCode() {
            // When & Then
            assertThatThrownBy(() -> Country.of(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("팩토리 메서드 테스트")
    class FactoryMethodTest {

        @Test
        @DisplayName("korea() 팩토리 메서드")
        void koreaFactory() {
            // When
            Country korea = Country.korea();

            // Then
            assertThat(korea.isKorea()).isTrue();
            assertThat(korea.getCode()).isEqualTo("KR");
        }

        @Test
        @DisplayName("us() 팩토리 메서드")
        void usFactory() {
            // When
            Country us = Country.us();

            // Then
            assertThat(us.isUs()).isTrue();
            assertThat(us.getCode()).isEqualTo("US");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 국가 코드는 동등")
        void equalsByValue() {
            // Given
            Country korea1 = Country.of("KR");
            Country korea2 = Country.korea();

            // Then - 다른 방식으로 생성해도 같은 코드면 같다!
            assertThat(korea1).isEqualTo(korea2);
            assertThat(korea1.hashCode()).isEqualTo(korea2.hashCode());
        }

        @Test
        @DisplayName("다른 국가 코드는 동등하지 않음")
        void notEqualsByDifferentValue() {
            // Given
            Country korea = Country.korea();
            Country us = Country.us();

            // Then
            assertThat(korea).isNotEqualTo(us);
        }
    }
}
