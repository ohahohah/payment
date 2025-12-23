package com.example.payment_ddd_v1_1.domain.repository;

import com.example.payment_ddd_v1_1.domain.model.Payment;

import java.util.List;
import java.util.Optional;

/**
 * PaymentRepository - 결제 저장소 인터페이스 (순수 Java)
 *
 * [정석 DDD - Domain 계층]
 * - 프레임워크 의존 없음
 * - JpaRepository 상속하지 않음
 * - 구현은 Infrastructure 계층에서
 */
public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    List<Payment> findAll();
}
