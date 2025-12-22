package com.example.payment_step2_2.entity;

import com.example.payment_step2_2.converter.CountryConverter;
import com.example.payment_step2_2.converter.MoneyConverter;
import com.example.payment_step2_2.domain.model.Country;
import com.example.payment_step2_2.domain.model.Money;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Payment - 결제 Aggregate Root (Rich Domain Model)
 *
 * ============================================================================
 * [payment_step1에서 변경된 점 - Aggregate 패턴 적용]
 * ============================================================================
 *
 * 1. setter 제거:
 *    - setStatus() 제거
 *    - setUpdatedAt() 제거
 *
 * 2. 비즈니스 메서드 추가:
 *    - complete(): PENDING -> COMPLETED
 *    - fail(): PENDING -> FAILED
 *    - refund(): COMPLETED -> REFUNDED
 *
 * 3. 상태 전이 규칙 캡슐화:
 *    - 잘못된 상태 전이 시 예외 발생
 *    - Service가 아닌 Entity가 규칙을 검증
 *
 * ============================================================================
 * [Aggregate Root란?]
 * ============================================================================
 *
 * - 관련된 도메인 객체들의 묶음(클러스터)의 진입점
 * - 외부에서는 반드시 Aggregate Root를 통해서만 내부 객체에 접근
 * - 트랜잭션 일관성의 경계
 *
 * ============================================================================
 * [Anemic vs Rich Domain Model]
 * ============================================================================
 *
 * Anemic (payment_step1):
 *   // Service에서
 *   if (payment.getStatus() != PaymentStatus.COMPLETED) {
 *       throw new IllegalStateException(...);
 *   }
 *   payment.setStatus(PaymentStatus.REFUNDED);
 *
 * Rich (payment_step2_2):
 *   // Service에서
 *   payment.refund();  // 내부에서 검증 + 상태 변경
 */
@Entity
@Table(name = "payments_step2_2")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false)
    private Money originalPrice;

    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false)
    private Money discountedAmount;

    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false)
    private Money taxedAmount;

    @Convert(converter = CountryConverter.class)
    @Column(nullable = false, length = 10)
    private Country country;

    @Column(nullable = false)
    private Boolean isVip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Payment() {
    }

    private Payment(Money originalPrice, Money discountedAmount, Money taxedAmount,
                    Country country, Boolean isVip) {
        this.originalPrice = originalPrice;
        this.discountedAmount = discountedAmount;
        this.taxedAmount = taxedAmount;
        this.country = country;
        this.isVip = isVip;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 결제 생성 팩토리 메서드
     */
    public static Payment create(Money originalPrice, Money discountedAmount,
                                  Money taxedAmount, Country country, Boolean isVip) {
        return new Payment(originalPrice, discountedAmount, taxedAmount, country, isVip);
    }

    // ==========================================================================
    // 비즈니스 메서드 - 상태 변경은 여기서만// (setter 없음)
    // ==========================================================================

    /**
     * 결제 완료 처리
     *
     * [변경 전 - payment_step1]
     * // Service에서
     * payment.setStatus(PaymentStatus.COMPLETED);
     * payment.setUpdatedAt(LocalDateTime.now());
     *
     * [변경 후 - payment_step2_2]
     * // Service에서
     * payment.complete();  // 검증 + 상태 변경이 Entity 안에
     */
    public void complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "대기 상태의 결제만 완료할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = PaymentStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 결제 실패 처리
     */
    public void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "대기 상태의 결제만 실패 처리할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = PaymentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 환불 처리
     *
     * [변경 전 - payment_step1]
     * // Service에서
     * if (payment.getStatus() != PaymentStatus.COMPLETED) {
     *     throw new IllegalStateException("완료된 결제만 환불할 수 있습니다");
     * }
     * payment.setStatus(PaymentStatus.REFUNDED);
     *
     * [변경 후 - payment_step2_2]
     * // Service에서
     * payment.refund();  // 검증 로직이 Entity 안에//
     */
    public void refund() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "완료된 결제만 환불할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();
    }

    // ==========================================================================
    // Getters (setter 없음//)
    // ==========================================================================

    public Long getId() { return id; }
    public Money getOriginalPrice() { return originalPrice; }
    public Money getDiscountedAmount() { return discountedAmount; }
    public Money getTaxedAmount() { return taxedAmount; }
    public Country getCountry() { return country; }
    public Boolean getIsVip() { return isVip; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
