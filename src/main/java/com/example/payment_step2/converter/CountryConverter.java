package com.example.payment_step2.converter;

import com.example.payment_step2.domain.model.Country;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * CountryConverter - Country Value Object를 DB String 컬럼으로 변환
 *
 * ============================================================================
 * [변환 흐름]
 * ============================================================================
 *
 * 저장 시: Country("KR") --convertToDatabaseColumn--> "KR" (DB VARCHAR)
 * 조회 시: "KR" (DB VARCHAR) --convertToEntityAttribute--> Country("KR")
 *
 * ============================================================================
 * [Country.of() 호출의 이점]
 * ============================================================================
 *
 * DB 조회 시 Country.of()를 호출하므로:
 * 1. 국가 코드가 대문자로 정규화됨 ("kr" -> "KR")
 * 2. 지원하지 않는 국가면 예외 발생
 * 3. DB에 잘못된 데이터가 있으면 조기에 발견
 */
@Converter(autoApply = true)
public class CountryConverter implements AttributeConverter<Country, String> {

    /**
     * Java -> DB 변환
     *
     * Country 객체의 code를 추출하여 String으로 저장
     *
     * @param country Country Value Object
     * @return DB에 저장할 String 값 (국가 코드)
     */
    @Override
    public String convertToDatabaseColumn(Country country) {
        return country == null ? null : country.getCode();
    }

    /**
     * DB -> Java 변환
     *
     * DB에서 조회한 String 값을 Country 객체로 변환
     * Country.of()에서 유효성 검증 및 정규화 수행
     *
     * @param code DB에서 조회한 국가 코드
     * @return Country Value Object
     */
    @Override
    public Country convertToEntityAttribute(String code) {
        return code == null ? null : Country.of(code);
    }
}
