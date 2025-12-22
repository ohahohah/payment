package com.example.payment_ul.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ====================================================================
 * Payment - 결제 엔티티 (유비쿼터스 랭귀지 적용)
 * ====================================================================
 *
 * [필드명 변경]
 * | 변경 전 | 변경 후           | 설명                    |
 * |---------|-------------------|-------------------------|
 * | amt1    | originalPrice     | 원래 가격               |
 * | amt2    | discountedAmount  | 할인 적용 후 금액       |
 * | amt3    | taxedAmount       | 세금 적용 후 최종 금액  |
 * | cd      | country           | 국가 코드               |
 * | flag    | isVip             | VIP 고객 여부           |
 * | stat    | status            | 결제 상태               |
 * | cdt     | createdAt         | 생성 일시               |
 * | udt     | updatedAt         | 수정 일시               |
 */
@Entity
@Table(name = "payments_ul")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double originalPrice;

    @Column(nullable = false)
    private Double discountedAmount;

    @Column(nullable = false)
    private Double taxedAmount;

    @Column(nullable = false, length = 10)
    private String country;

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

    private Payment(Double originalPrice, Double discountedAmount, Double taxedAmount,
                    String country, Boolean isVip) {
        this.originalPrice = originalPrice;
        this.discountedAmount = discountedAmount;
        this.taxedAmount = taxedAmount;
        this.country = country;
        this.isVip = isVip;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Payment create(Double originalPrice, Double discountedAmount,
                                  Double taxedAmount, String country, Boolean isVip) {
        return new Payment(originalPrice, discountedAmount, taxedAmount, country, isVip);
    }

    // Getters
    public Long getId() { return id; }
    public Double getOriginalPrice() { return originalPrice; }
    public Double getDiscountedAmount() { return discountedAmount; }
    public Double getTaxedAmount() { return taxedAmount; }
    public String getCountry() { return country; }
    public Boolean getIsVip() { return isVip; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setStatus(PaymentStatus status) { this.status = status; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
