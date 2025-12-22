package com.example.payment_ul.repository;

import com.example.payment_ul.entity.Payment;
import com.example.payment_ul.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ====================================================================
 * PaymentRepository - 결제 저장소 (유비쿼터스 랭귀지 적용)
 * ====================================================================
 *
 * [독립 패키지]
 * - payment_ul 패키지 전용 Repository입니다
 * - Qualifier 없이 독립적으로 동작합니다
 *
 * [메서드명 변경]
 * | 변경 전                    | 변경 후                      |
 * |----------------------------|------------------------------|
 * | findByStat()               | findByStatus()               |
 * | findByCd()                 | findByCountry()              |
 * | sumAmt3ByStat()            | sumTaxedAmountByStatus()     |
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByCountry(String country);

    List<Payment> findByStatusAndCountry(PaymentStatus status, String country);

    List<Payment> findByTaxedAmountGreaterThan(Double amount);

    List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Payment> findByIsVipAndStatusOrderByCreatedAtDesc(Boolean isVip, PaymentStatus status);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.country = :country")
    long countByCountry(@Param("country") String country);

    @Query("SELECT COALESCE(SUM(p.taxedAmount), 0) FROM Payment p WHERE p.status = :status")
    Double sumTaxedAmountByStatus(@Param("status") PaymentStatus status);

    @Query(value = "SELECT * FROM payments_ul ORDER BY created_at DESC LIMIT :limit",
           nativeQuery = true)
    List<Payment> findRecentPayments(@Param("limit") int limit);
}
