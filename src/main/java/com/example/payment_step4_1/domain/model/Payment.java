package com.example.payment_step4_1.domain.model;

import java.time.LocalDateTime;

/**
 * Payment - 결제 Aggregate Root
 *
 * [기존 책임]
 * - 결제 생성, 완료, 실패, 환불 상태 관리
 * - 비즈니스 규칙 캡슐화
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

    public static Payment create(Money originalPrice, Money discountedAmount,
                                  Money taxedAmount, Country country, boolean isVip) {
        return new Payment(
                null,
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

    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("ID는 한 번만 할당할 수 있습니다");
        }
        this.id = id;
    }
}
