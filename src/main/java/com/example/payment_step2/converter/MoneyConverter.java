package com.example.payment_step2.converter;

import com.example.payment_step2.domain.model.Money;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * MoneyConverter - Money Value Object를 DB Double 컬럼으로 변환
 *
 * ============================================================================
 * [@Convert vs @Embedded]
 * ============================================================================
 *
 * @Convert (이 프로젝트에서 사용):
 * - 단일 값(double)으로 표현 가능한 Value Object에 적합
 * - Money -> Double로 변환하여 단일 컬럼에 저장
 * - 기존 DB 스키마와 호환성 유지
 *
 * @Embedded:
 * - 여러 필드를 가진 Value Object에 적합 (예: Address)
 * - Value Object의 필드가 각각 컬럼이 됨
 *
 * ============================================================================
 * [변환 흐름]
 * ============================================================================
 *
 * 저장 시: Money(10000.0) --convertToDatabaseColumn--> 10000.0 (DB DOUBLE)
 * 조회 시: 10000.0 (DB DOUBLE) --convertToEntityAttribute--> Money(10000.0)
 *
 * ============================================================================
 * [autoApply = true]
 * ============================================================================
 *
 * Entity에서 @Convert 어노테이션 없이도 Money 타입 필드에 자동 적용됨
 * 단, 명시적으로 @Convert를 사용하면 어떤 Converter를 쓰는지 명확해짐
 */
@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, Double> {

    /**
     * Java -> DB 변환
     *
     * Entity의 Money 필드를 DB에 저장할 때 호출됨
     * Money 객체에서 amount 값을 추출하여 Double로 저장
     *
     * @param money Money Value Object
     * @return DB에 저장할 Double 값
     */
    @Override
    public Double convertToDatabaseColumn(Money money) {
        return money == null ? null : money.getAmount();
    }

    /**
     * DB -> Java 변환
     *
     * DB에서 조회한 Double 값을 Money 객체로 변환
     * Money.of()를 통해 유효성 검증이 자동으로 수행됨
     *
     * @param amount DB에서 조회한 Double 값
     * @return Money Value Object
     */
    @Override
    public Money convertToEntityAttribute(Double amount) {
        return amount == null ? null : Money.of(amount);
    }
}
