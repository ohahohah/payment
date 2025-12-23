package com.example.payment_ddd_v1_1.infrastructure.mapper;

import com.example.payment_ddd_v1_1.domain.model.Country;
import com.example.payment_ddd_v1_1.domain.model.Money;
import com.example.payment_ddd_v1_1.domain.model.Payment;
import com.example.payment_ddd_v1_1.domain.model.PaymentStatus;
import com.example.payment_ddd_v1_1.infrastructure.persistence.PaymentJpaEntity;
import org.springframework.stereotype.Component;

/**
 * PaymentMapper - Domain ↔ JPA Entity 변환기
 *
 * ============================================================================
 * [정석 DDD - Infrastructure 계층]
 * ============================================================================
 *
 * Domain 모델(Payment)과 JPA Entity(PaymentJpaEntity) 간의 변환 담당
 *
 * ============================================================================
 * [변환 방향]
 * ============================================================================
 *
 * 1. toDomain(): PaymentJpaEntity → Payment
 *    - DB에서 조회한 데이터를 Domain 객체로 변환
 *    - Payment.reconstitute() 사용 (기존 객체 복원)
 *
 * 2. toEntity(): Payment → PaymentJpaEntity
 *    - Domain 객체를 DB 저장용으로 변환
 *    - Value Object를 원시 타입으로 풀어냄
 *
 * ============================================================================
 * [왜 필요한가?]
 * ============================================================================
 *
 * - Domain의 Payment: Money, Country 등 Value Object 사용
 * - JPA의 Entity: Double, String 등 원시 타입 사용
 * - 이 차이를 Mapper가 연결
 */
@Component
public class PaymentMapper {

    /**
     * JPA Entity → Domain 모델
     */
    public Payment toDomain(PaymentJpaEntity entity) {
        return Payment.reconstitute(
                entity.getId(),
                Money.of(entity.getOriginalPrice()),
                Money.of(entity.getDiscountedAmount()),
                Money.of(entity.getTaxedAmount()),
                Country.of(entity.getCountry()),
                entity.getVip(),
                PaymentStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Domain 모델 → JPA Entity
     */
    public PaymentJpaEntity toEntity(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setId(payment.getId());
        entity.setOriginalPrice(payment.getOriginalPrice().getAmount());
        entity.setDiscountedAmount(payment.getDiscountedAmount().getAmount());
        entity.setTaxedAmount(payment.getTaxedAmount().getAmount());
        entity.setCountry(payment.getCountry().getCode());
        entity.setVip(payment.isVip());
        entity.setStatus(payment.getStatus().name());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
        return entity;
    }
}
