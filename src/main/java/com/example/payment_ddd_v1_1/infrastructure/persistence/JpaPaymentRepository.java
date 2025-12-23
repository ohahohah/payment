package com.example.payment_ddd_v1_1.infrastructure.persistence;

import com.example.payment_ddd_v1_1.domain.model.Payment;
import com.example.payment_ddd_v1_1.domain.repository.PaymentRepository;
import com.example.payment_ddd_v1_1.infrastructure.mapper.PaymentMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JpaPaymentRepository - PaymentRepository 구현체
 *
 * ============================================================================
 * [정석 DDD - Infrastructure 계층]
 * ============================================================================
 *
 * 흐름:
 * 1. Domain의 Payment 객체를 받음
 * 2. Mapper로 PaymentJpaEntity로 변환
 * 3. Spring Data JPA로 저장
 * 4. 다시 Mapper로 Payment로 변환하여 반환
 *
 * ============================================================================
 * [payment_ddd_v1과의 차이점]
 * ============================================================================
 *
 * payment_ddd_v1:
 *   Spring Data JPA를 직접 사용
 *   변환 없이 바로 저장
 *
 * payment_ddd_v1_1 (정석):
 *   Payment → PaymentJpaEntity → DB → PaymentJpaEntity → Payment
 *   Mapper를 통한 변환 필요
 *
 * ============================================================================
 * [장점과 단점]
 * ============================================================================
 *
 * 장점:
 * - Domain 순수성 유지
 * - 기술 교체 시 여기만 수정
 *
 * 단점:
 * - 변환 비용 (성능)
 * - 코드량 증가
 */
@Repository
public class JpaPaymentRepository implements PaymentRepository {

    private final SpringDataPaymentRepository springDataRepository;
    private final PaymentMapper mapper;

    public JpaPaymentRepository(SpringDataPaymentRepository springDataRepository,
                                 PaymentMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Payment save(Payment payment) {
        // 1. Domain → JPA Entity
        PaymentJpaEntity entity = mapper.toEntity(payment);

        // 2. JPA로 저장
        PaymentJpaEntity savedEntity = springDataRepository.save(entity);

        // 3. JPA Entity → Domain (ID가 할당된 상태)
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return springDataRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Payment> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
