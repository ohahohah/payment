package com.example.payment_ddd.domain.model;

import java.util.Objects;
import java.util.Set;

/**
 * Country - 국가 Value Object
 *
 * [Value Object 특징]
 * - 불변, 동등성, 자가 검증
 *
 * [왜 String 대신 Country를 사용하나요?]
 * - 유효한 국가 코드만 허용 (자가 검증)
 * - 국가별 비즈니스 로직을 캡슐화 (isKorea, isUs)
 * - 타입 안전성 (String country vs Country country)
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

    /**
     * 정적 팩토리 메서드
     */
    public static Country of(String code) {
        return new Country(code);
    }

    /**
     * 한국
     */
    public static Country korea() {
        return new Country("KR");
    }

    /**
     * 미국
     */
    public static Country us() {
        return new Country("US");
    }

    /**
     * 국가 코드 반환
     */
    public String getCode() {
        return code;
    }

    /**
     * 한국인지 확인
     */
    public boolean isKorea() {
        return "KR".equals(code);
    }

    /**
     * 미국인지 확인
     */
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
