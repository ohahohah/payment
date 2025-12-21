package com.example.payment_ddd.infrastructure.persistence;

import com.example.payment_ddd.domain.model.*;
import com.example.payment_ddd.domain.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JpaPaymentRepository - 도메인 Repository 구현체
 *
 * [Repository 패턴 구현]
 * - 도메인 레이어의 PaymentRepository 인터페이스 구현
 * - 도메인 모델 ↔ JPA 엔티티 매핑 담당
 *
 * [의존성 방향]
 * - Infrastructure → Domain (O)
 * - Domain → Infrastructure (X)
 *
 * [이 클래스의 책임]
 * 1. 도메인 객체를 JPA 엔티티로 변환 (저장)
 * 2. JPA 엔티티를 도메인 객체로 변환 (조회)
 * 3. JPA Repository 호출
 */
@Repository
public class JpaPaymentRepository implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    public JpaPaymentRepository(PaymentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = toEntity(payment);
        PaymentJpaEntity savedEntity = jpaRepository.save(entity);

        // ID 할당 (새로 생성된 경우)
        if (payment.getId() == null) {
            payment.assignId(savedEntity.getId());
        }

        return payment;
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return jpaRepository.findByStatus(status.name())
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    /**
     * 도메인 → JPA 엔티티 변환
     */
    private PaymentJpaEntity toEntity(Payment payment) {
        return new PaymentJpaEntity(
                payment.getId(),
                payment.getOriginalPrice().getAmount(),
                payment.getDiscountedAmount().getAmount(),
                payment.getTaxedAmount().getAmount(),
                payment.getCountry().getCode(),
                payment.isVip(),
                payment.getStatus().name(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    /**
     * JPA 엔티티 → 도메인 변환
     */
    private Payment toDomain(PaymentJpaEntity entity) {
        return Payment.reconstitute(
                entity.getId(),
                Money.of(entity.getOriginalPrice()),
                Money.of(entity.getDiscountedAmount()),
                Money.of(entity.getTaxedAmount()),
                Country.of(entity.getCountry()),
                entity.isVip(),
                PaymentStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
