package com.example.payment_ddd_v1.infrastructure.converter;

import com.example.payment_ddd_v1.domain.model.Country;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * CountryConverter - Country Value Object를 DB String 컬럼으로 변환
 *
 * [Infrastructure 계층]
 * - Domain의 Value Object를 DB에 저장하기 위한 변환기
 * - Domain 계층은 이 클래스에 의존하지 않음 (의존성 역전)
 */
@Converter(autoApply = false)
public class CountryConverter implements AttributeConverter<Country, String> {

    @Override
    public String convertToDatabaseColumn(Country country) {
        return country == null ? null : country.getCode();
    }

    @Override
    public Country convertToEntityAttribute(String code) {
        return code == null ? null : Country.of(code);
    }
}
