package com.example.payment_ddd_v1_1.domain.model;

import java.time.LocalDateTime;

/**
 * Payment - 결제 Aggregate Root (순수 Java)
 *
 * ============================================================================
 * [정석 DDD - 순수 도메인 엔티티]
 * ============================================================================
 *
 * - @Entity 없음 (JPA 의존 없음)
 * - @Column 없음
 * - 프레임워크에 독립적
 * - 순수 비즈니스 로직만 포함
 *
 * ============================================================================
 * [payment_ddd_v1과의 차이점]
 * ============================================================================
 *
 * payment_ddd_v1:
 *   @Entity
 *   @Table(name = "payments")
 *   public class Payment { ... }  // JPA 의존
 *
 * payment_ddd_v1_1 (정석):
 *   public class Payment { ... }  // 순수 Java
 *
 * JPA 매핑은 Infrastructure 계층의 PaymentJpaEntity가 담당
 *
 * ============================================================================
 * [장점]
 * ============================================================================
 *
 * 1. 도메인 순수성: 기술 변경에 영향받지 않음
 * 2. 테스트 용이성: JPA 없이 단위 테스트 가능
 * 3. 유연성: JPA → MongoDB 등 기술 교체 시 도메인 수정 없음
 *
 * [단점]
 * - 코드량 증가 (JpaEntity + Mapper 필요)
 * - 변환 비용 (성능)
 */
public class Payment {

    private Long id;
    private final Money originalPrice;
    private final Money discountedAmount;
    private final Money taxedAmount;
    private final Country country;
    private final boolean vip;
    private PaymentStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Payment(Long id, Money originalPrice, Money discountedAmount, Money taxedAmount,
                    Country country, boolean vip, PaymentStatus status,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.originalPrice = originalPrice;
        this.discountedAmount = discountedAmount;
        this.taxedAmount = taxedAmount;
        this.country = country;
        this.vip = vip;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 새 결제 생성 (팩토리 메서드)
     */
    public static Payment create(Money originalPrice, Money discountedAmount,
                                  Money taxedAmount, Country country, boolean isVip) {
        return new Payment(
                null,  // ID는 저장 시 할당
                originalPrice,
                discountedAmount,
                taxedAmount,
                country,
                isVip,
                PaymentStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    /**
     * 기존 결제 복원 (Repository에서 사용)
     */
    public static Payment reconstitute(Long id, Money originalPrice, Money discountedAmount,
                                        Money taxedAmount, Country country, boolean vip,
                                        PaymentStatus status, LocalDateTime createdAt,
                                        LocalDateTime updatedAt) {
        return new Payment(id, originalPrice, discountedAmount, taxedAmount,
                country, vip, status, createdAt, updatedAt);
    }

    // ==========================================================================
    // 비즈니스 메서드
    // ==========================================================================

    public void complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "대기 상태의 결제만 완료할 수 있습니다. 현재: " + this.status);
        }
        this.status = PaymentStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "대기 상태의 결제만 실패 처리할 수 있습니다. 현재: " + this.status);
        }
        this.status = PaymentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    public void refund() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "완료된 결제만 환불할 수 있습니다. 현재: " + this.status);
        }
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();
    }

    // ==========================================================================
    // Getters
    // ==========================================================================

    public Long getId() { return id; }
    public Money getOriginalPrice() { return originalPrice; }
    public Money getDiscountedAmount() { return discountedAmount; }
    public Money getTaxedAmount() { return taxedAmount; }
    public Country getCountry() { return country; }
    public boolean isVip() { return vip; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /**
     * ID 할당 (Repository 전용)
     */
    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("ID는 한 번만 할당할 수 있습니다");
        }
        this.id = id;
    }
}
