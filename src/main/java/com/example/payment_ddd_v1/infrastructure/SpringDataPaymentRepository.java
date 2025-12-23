package com.example.payment_ddd_v1.infrastructure;

import com.example.payment_ddd_v1.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SpringDataPaymentRepository - Spring Data JPA Repository
 *
 * [Infrastructure 계층]
 * - Spring Data JPA가 자동으로 구현체 생성
 * - JpaPaymentRepository에서 위임받아 사용
 */
interface SpringDataPaymentRepository extends JpaRepository<Payment, Long> {
}
