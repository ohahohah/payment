package com.example.payment_step2_2.converter;

import com.example.payment_step2_2.domain.model.Money;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * MoneyConverter - Money Value Object를 DB Double 컬럼으로 변환
 *
 * [payment_step1과 동일]
 */
@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, Double> {

    @Override
    public Double convertToDatabaseColumn(Money money) {
        return money == null ? null : money.getAmount();
    }

    @Override
    public Money convertToEntityAttribute(Double amount) {
        return amount == null ? null : Money.of(amount);
    }
}
