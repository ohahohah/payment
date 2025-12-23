package com.example.payment_ddd_v1_1.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SpringDataPaymentRepository - Spring Data JPA Repository
 *
 * [Infrastructure 계층]
 * - JpaPaymentRepository 내부에서만 사용
 * - 외부에 노출하지 않음 (package-private)
 */
interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, Long> {
}
