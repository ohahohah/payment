package com.example.payment_step4_1.domain.model;

import java.util.Objects;

/**
 * Country - 국가 Value Object
 */
public class Country {

    private final String code;

    private Country(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("국가 코드는 필수입니다");
        }
        this.code = code.toUpperCase();
    }

    public static Country of(String code) {
        return new Country(code);
    }

    public String getCode() {
        return code;
    }

    public boolean isKorea() {
        return "KR".equals(code);
    }

    public boolean isUS() {
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
