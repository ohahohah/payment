package com.example.payment_step2.entity;

import com.example.payment_step2.converter.CountryConverter;
import com.example.payment_step2.converter.MoneyConverter;
import com.example.payment_step2.domain.model.Country;
import com.example.payment_step2.domain.model.Money;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Payment - 결제 엔티티 (Value Object 적용)
 *
 * ============================================================================
 * [payment_ul에서 변경된 점 - Value Object만 적용]
 * ============================================================================
 *
 * 1. 타입 변경:
 *    - Double originalPrice -> Money originalPrice
 *    - String country -> Country country
 *
 * 2. JPA 매핑 방식:
 *    - @Convert 어노테이션으로 Value Object를 DB 타입으로 변환
 *    - DB 스키마는 payment_ul과 동일하게 유지
 *
 * [변경되지 않은 점]
 * - Entity 구조는 payment_ul과 동일 (Anemic Domain Model)
 * - setter를 통한 상태 변경 방식 유지
 * - 비즈니스 로직은 Service에서 처리
 *
 * ============================================================================
 * [@Convert 사용 이유]
 * ============================================================================
 *
 * Money와 Country는 단일 값으로 표현 가능하므로 @Convert 사용
 * - Money -> Double 컬럼 하나
 * - Country -> VARCHAR 컬럼 하나
 *
 * @Embedded는 여러 필드를 가진 Value Object에 적합 (예: Address)
 */
@Entity
@Table(name = "payments_step1")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 원래 가격
     *
     * [변경 전] Double originalPrice
     * [변경 후] Money originalPrice + @Convert
     *
     * MoneyConverter가 Money <-> Double 변환 수행
     * DB에는 Double로 저장됨
     */
    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false)
    private Money originalPrice;

    /**
     * 할인 적용 후 금액
     */
    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false)
    private Money discountedAmount;

    /**
     * 세금 적용 후 최종 금액
     */
    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false)
    private Money taxedAmount;

    /**
     * 국가
     *
     * [변경 전] String country
     * [변경 후] Country country + @Convert
     *
     * CountryConverter가 Country <-> String 변환 수행
     * DB에는 VARCHAR로 저장됨
     */
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
     *
     * [변경 전] Payment.create(Double, Double, Double, String, Boolean)
     * [변경 후] Payment.create(Money, Money, Money, Country, Boolean)
     *
     * Value Object를 받아서 타입 안전성 보장
     */
    public static Payment create(Money originalPrice, Money discountedAmount,
                                  Money taxedAmount, Country country, Boolean isVip) {
        return new Payment(originalPrice, discountedAmount, taxedAmount, country, isVip);
    }

    // Getters
    public Long getId() { return id; }
    public Money getOriginalPrice() { return originalPrice; }
    public Money getDiscountedAmount() { return discountedAmount; }
    public Money getTaxedAmount() { return taxedAmount; }
    public Country getCountry() { return country; }
    public Boolean getIsVip() { return isVip; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters (payment_ul과 동일하게 유지)
    public void setStatus(PaymentStatus status) { this.status = status; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
