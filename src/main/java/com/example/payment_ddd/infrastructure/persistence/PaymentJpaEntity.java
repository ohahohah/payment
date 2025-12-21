package com.example.payment_ddd.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * PaymentJpaEntity - JPA 엔티티 (인프라 레이어)
 *
 * [도메인 모델 vs 영속성 모델 분리]
 * - 도메인 모델(Payment): 비즈니스 로직 포함, JPA 의존성 없음
 * - 영속성 모델(PaymentJpaEntity): 순수 데이터 저장용, JPA 어노테이션 포함
 *
 * [왜 분리하나요?]
 * 1. 도메인 순수성: 도메인이 인프라(JPA)에 의존하지 않음
 * 2. 유연성: ORM 변경 시 도메인 영향 없음
 * 3. 테스트 용이성: 도메인 테스트 시 DB 불필요
 *
 * [단점]
 * - 매핑 코드 필요 (toEntity, toDomain)
 * - 작은 프로젝트에서는 오버헤드
 */
@Entity
@Table(name = "payments_ddd")
public class PaymentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double originalPrice;

    @Column(nullable = false)
    private double discountedAmount;

    @Column(nullable = false)
    private double taxedAmount;

    @Column(nullable = false, length = 10)
    private String country;

    @Column(nullable = false)
    private boolean vip;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected PaymentJpaEntity() {
        // JPA 기본 생성자
    }

    public PaymentJpaEntity(Long id, double originalPrice, double discountedAmount,
                             double taxedAmount, String country, boolean vip,
                             String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
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

    // Getters
    public Long getId() { return id; }
    public double getOriginalPrice() { return originalPrice; }
    public double getDiscountedAmount() { return discountedAmount; }
    public double getTaxedAmount() { return taxedAmount; }
    public String getCountry() { return country; }
    public boolean isVip() { return vip; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters for JPA
    public void setId(Long id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
