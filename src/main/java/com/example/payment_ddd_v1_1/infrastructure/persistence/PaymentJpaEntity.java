package com.example.payment_ddd_v1_1.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * PaymentJpaEntity - JPA 전용 엔티티
 *
 * ============================================================================
 * [정석 DDD - Infrastructure 계층]
 * ============================================================================
 *
 * - JPA 어노테이션은 여기에만 존재
 * - Domain의 Payment와 별도로 존재
 * - PaymentMapper를 통해 Payment ↔ PaymentJpaEntity 변환
 *
 * ============================================================================
 * [왜 분리하나?]
 * ============================================================================
 *
 * 1. Domain 순수성 유지
 *    - Payment.java는 순수 Java
 *    - JPA 기술 변경 시 Domain 영향 없음
 *
 * 2. 매핑 유연성
 *    - DB 스키마와 Domain 모델이 다를 수 있음
 *    - 예: DB에는 정규화, Domain에는 비정규화
 *
 * 3. 기술 교체 용이
 *    - JPA → MongoDB: 이 클래스만 교체
 *    - Domain 코드 수정 없음
 */
@Entity
@Table(name = "payments_ddd_v1_1")
public class PaymentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_price", nullable = false)
    private Double originalPrice;

    @Column(name = "discounted_amount", nullable = false)
    private Double discountedAmount;

    @Column(name = "taxed_amount", nullable = false)
    private Double taxedAmount;

    @Column(nullable = false, length = 10)
    private String country;

    @Column(nullable = false)
    private Boolean vip;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public PaymentJpaEntity() {
    }

    // ==========================================================================
    // Getters & Setters (JPA용)
    // ==========================================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(Double originalPrice) { this.originalPrice = originalPrice; }

    public Double getDiscountedAmount() { return discountedAmount; }
    public void setDiscountedAmount(Double discountedAmount) { this.discountedAmount = discountedAmount; }

    public Double getTaxedAmount() { return taxedAmount; }
    public void setTaxedAmount(Double taxedAmount) { this.taxedAmount = taxedAmount; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Boolean getVip() { return vip; }
    public void setVip(Boolean vip) { this.vip = vip; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
