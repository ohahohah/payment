package com.example.payment_ddd_v1.infrastructure.converter;

import com.example.payment_ddd_v1.domain.model.Money;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * MoneyConverter - Money Value Object를 DB Double 컬럼으로 변환
 *
 * [Infrastructure 계층]
 * - Domain의 Value Object를 DB에 저장하기 위한 변환기
 * - Domain 계층은 이 클래스에 의존하지 않음 (의존성 역전)
 *
 * [@Converter 방식 선택 이유]
 * - Money는 단일 필드(amount)만 가지므로 @Convert가 적합
 * - Value Object의 불변성(final)을 유지 가능
 * - @Embeddable은 기본 생성자, non-final 필드가 필요
 */
@Converter(autoApply = false)
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
