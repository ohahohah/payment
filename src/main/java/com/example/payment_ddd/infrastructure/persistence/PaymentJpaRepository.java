package com.example.payment_ddd.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PaymentJpaRepository - Spring Data JPA Repository
 *
 * [인프라 레이어]
 * - JPA 기술에 의존
 * - 도메인 Repository 인터페이스와 별개
 */
@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, Long> {

    List<PaymentJpaEntity> findByStatus(String status);
}
