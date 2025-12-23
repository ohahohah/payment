package com.example.payment_ddd_v1.domain.model;

import com.example.payment_ddd_v1.infrastructure.converter.CountryConverter;
import com.example.payment_ddd_v1.infrastructure.converter.MoneyConverter;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Payment - 결제 Aggregate Root (Rich Domain Model)
 *
 * ============================================================================
 * [DDD 계층형 아키텍처에서의 위치]
 * ============================================================================
 *
 * Domain 계층의 핵심 엔티티
 * - 비즈니스 로직 캡슐화 (complete(), refund())
 * - 상태 전이 규칙 보호
 * - Value Object 사용 (Money, Country)
 *
 * ============================================================================
 * [Rich Domain Model]
 * ============================================================================
 *
 * - 데이터 + 비즈니스 로직을 함께 보유
 * - 상태 변경은 의미있는 메서드로만 가능 (setter 없음)
 * - 도메인 규칙이 엔티티 안에 캡슐화
 *
 * ============================================================================
 * [JPA 매핑]
 * ============================================================================
 *
 * - @Entity: JPA 엔티티로 등록
 * - @Convert: Value Object를 DB 컬럼에 매핑
 *   - Money -> Double (MoneyConverter)
 *   - Country -> String (CountryConverter)
 *
 * [주의] Domain 계층이 Infrastructure(Converter)에 의존하는 것은
 * 순수 DDD 관점에서는 위반이지만, 실용적 선택으로 허용
 * 완전한 분리가 필요하면 별도 JPA Entity 클래스 생성 권장
 */
@Entity
@Table(name = "payments_ddd_v1")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = MoneyConverter.class)
    @Column(name = "original_price", nullable = false)
    private Money originalPrice;

    @Convert(converter = MoneyConverter.class)
    @Column(name = "discounted_amount", nullable = false)
    private Money discountedAmount;

    @Convert(converter = MoneyConverter.class)
    @Column(name = "taxed_amount", nullable = false)
    private Money taxedAmount;

    @Convert(converter = CountryConverter.class)
    @Column(nullable = false, length = 10)
    private Country country;

    @Column(nullable = false)
    private boolean vip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA용 기본 생성자 (protected)
     * - JPA는 리플렉션으로 객체 생성 시 기본 생성자 필요
     * - protected로 외부 직접 호출 방지
     */
    protected Payment() {
    }

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

    /**
     * 팩토리 메서드 - 결제 생성
     *
     * [왜 생성자 대신 팩토리 메서드?]
     * - 의미있는 이름으로 생성 의도 표현
     * - 생성 로직 캡슐화 (향후 유효성 검증 추가 용이)
     * - 다양한 생성 방식 지원 가능
     */
    public static Payment create(Money originalPrice, Money discountedAmount,
                                  Money taxedAmount, Country country, boolean isVip) {
        return new Payment(originalPrice, discountedAmount, taxedAmount, country, isVip);
    }

    // ==========================================================================
    // 비즈니스 메서드 - 상태 변경은 여기서만! (setter 없음)
    // ==========================================================================

    /**
     * 결제 완료 처리
     *
     * [비즈니스 규칙]
     * - PENDING 상태에서만 완료 가능
     * - 상태 전이: PENDING -> COMPLETED
     */
    public void complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "대기 상태의 결제만 완료할 수 있습니다. 현재: " + this.status);
        }
        this.status = PaymentStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 결제 실패 처리
     *
     * [비즈니스 규칙]
     * - PENDING 상태에서만 실패 처리 가능
     * - 상태 전이: PENDING -> FAILED
     */
    public void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "대기 상태의 결제만 실패 처리할 수 있습니다. 현재: " + this.status);
        }
        this.status = PaymentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 환불 처리
     *
     * [비즈니스 규칙]
     * - COMPLETED 상태에서만 환불 가능
     * - 상태 전이: COMPLETED -> REFUNDED
     */
    public void refund() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "완료된 결제만 환불할 수 있습니다. 현재: " + this.status);
        }
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();
    }

    // ==========================================================================
    // Getters (Setter 없음!)
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
}
