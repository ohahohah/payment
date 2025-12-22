package com.example.payment_step2_2.repository;

import com.example.payment_step2_2.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * PaymentRepository - 결제 저장소
 *
 * [payment_step1과 동일]
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
