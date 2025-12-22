package com.example.payment_step2_2.converter;

import com.example.payment_step2_2.domain.model.Country;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * CountryConverter - Country Value Object를 DB String 컬럼으로 변환
 *
 * [payment_step1과 동일]
 */
@Converter(autoApply = true)
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
