package com.example.payment_step2.repository;

import com.example.payment_step2.entity.Payment;
import com.example.payment_step2.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PaymentRepository - 결제 저장소 인터페이스
 *
 * [payment_ul과 동일]
 * - Spring Data JPA 사용
 * - @Convert 덕분에 DB 스키마 변경 없이 Value Object 사용 가능
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT SUM(p.taxedAmount) FROM Payment p WHERE p.status = :status")
    Double sumTaxedAmountByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT p FROM Payment p ORDER BY p.createdAt DESC LIMIT :limit")
    List<Payment> findRecentPayments(@Param("limit") int limit);
}
