package com.example.payment_step2_2.domain.model;

import java.util.Objects;
import java.util.Set;

/**
 * Country - 국가 Value Object
 *
 * [payment_step1과 동일]
 * - 불변 객체
 * - 자가 검증 (지원 국가만 허용)
 * - 자동 정규화 (대문자 변환)
 */
public class Country {

    private static final Set<String> SUPPORTED_COUNTRIES = Set.of("KR", "US");

    private final String code;

    private Country(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("국가 코드는 필수입니다");
        }
        String upperCode = code.toUpperCase();
        if (!SUPPORTED_COUNTRIES.contains(upperCode)) {
            throw new IllegalArgumentException("지원하지 않는 국가입니다: " + code);
        }
        this.code = upperCode;
    }

    public static Country of(String code) {
        return new Country(code);
    }

    public static Country korea() {
        return new Country("KR");
    }

    public static Country us() {
        return new Country("US");
    }

    public String getCode() {
        return code;
    }

    public boolean isKorea() {
        return "KR".equals(code);
    }

    public boolean isUs() {
        return "US".equals(code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country country = (Country) o;
        return Objects.equals(code, country.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "Country(" + code + ")";
    }
}
