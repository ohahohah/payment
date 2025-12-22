package com.example.payment_ddd_v1.domain.model;

import java.util.Objects;
import java.util.Set;

/**
 * Country - 국가 Value Object
 *
 * [Value Object 특징]
 * 1. 불변(Immutable) - setter 없음
 * 2. 동등성(Equality) - 값이 같으면 같은 객체
 * 3. 자가 검증 - 생성 시 유효성 검증
 *
 * [왜 String 대신 Country?]
 * - String: "KOREA", "", "kr" 등 잘못된 값 허용
 * - Country: 유효한 국가 코드만 허용, 자동 정규화
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
        return code;
    }
}
