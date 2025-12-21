package com.example.payment_step1.domain.model;

import java.util.Objects;
import java.util.Set;

/**
 * Country - 국가 Value Object
 *
 * ============================================================================
 * [Anti-DDD에서 DDD로: String → Country]
 * ============================================================================
 *
 * Anti-DDD (기존):
 *   private String country;
 *   if (country.equals("KR")) { ... }
 *
 * DDD (개선):
 *   private final Country country;
 *   if (country.isKorea()) { ... }
 *
 * ============================================================================
 * [왜 String 대신 Country를 사용하나요?]
 * ============================================================================
 *
 * 문제 1: 유효하지 않은 값 허용
 *   - String country = "KOREA";  // 유효하지 않지만 허용됨
 *   - String country = "";       // 빈 문자열도 허용됨
 *   - Country country = Country.of("KOREA"); // 예외 발생!
 *
 * 문제 2: 비즈니스 로직 분산
 *   - if (country.equals("KR")) { ... } // 여기저기 분산
 *   - if (country.isKorea()) { ... }    // 로직이 객체 안에!
 *
 * 문제 3: 오타 및 대소문자 문제
 *   - if (country.equals("kr")) { ... } // 오타로 인한 버그
 *   - Country.of("kr") → 자동으로 "KR"로 정규화
 */
public class Country {

    private static final Set<String> SUPPORTED_COUNTRIES = Set.of("KR", "US");

    private final String code;

    /**
     * private 생성자 - 자가 검증
     */
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
     * 한국 팩토리 메서드
     *
     * 사용 예: Country.korea()
     */
    public static Country korea() {
        return new Country("KR");
    }

    /**
     * 미국 팩토리 메서드
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

    // ==========================================================================
    // 비즈니스 메서드 - 국가 판별
    // ==========================================================================

    /**
     * 한국인지 확인
     *
     * [비교]
     * Anti-DDD: country.equals("KR")
     * DDD:      country.isKorea()
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

    // ==========================================================================
    // Value Object의 핵심: equals, hashCode
    // ==========================================================================

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
