package com.example.payment_ddd_v1.domain.model;

import java.time.LocalDateTime;

/**
 * Payment - 결제 엔티티 (Rich Domain Model)
 *
 * [Rich Domain Model]
 * - 데이터 + 비즈니스 로직을 함께 보유
 * - 상태 변경은 의미있는 메서드로만 가능 (setter 없음)
 * - 도메인 규칙이 엔티티 안에 캡슐화
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

    private Payment(Money originalPrice, Money discountedAmount, Money taxedAmount,
                    Country country, boolean vip) {
        this.originalPrice = originalPrice;
        this.discountedAmount = discountedAmount;
        this.taxedAmount = taxedAmount;
        this.country = country;
        this.vip = vip;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Payment create(Money originalPrice, Money discountedAmount,
                                  Money taxedAmount, Country country, boolean isVip) {
        return new Payment(originalPrice, discountedAmount, taxedAmount, country, isVip);
    }

    /**
     * 결제 완료 - 비즈니스 규칙 캡슐화
     */
    public void complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 결제만 완료할 수 있습니다. 현재: " + this.status);
        }
        this.status = PaymentStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 환불 처리 - 비즈니스 규칙 캡슐화
     */
    public void refund() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 결제만 환불할 수 있습니다. 현재: " + this.status);
        }
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters (Setter 없음!)
    public Long getId() { return id; }
    public Money getOriginalPrice() { return originalPrice; }
    public Money getDiscountedAmount() { return discountedAmount; }
    public Money getTaxedAmount() { return taxedAmount; }
    public Country getCountry() { return country; }
    public boolean isVip() { return vip; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ID 설정 (Repository 전용)
    public void setId(Long id) { this.id = id; }
}
