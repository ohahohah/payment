package com.example.payment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ====================================================================
 * Payment - 결제 엔티티 (JPA Entity)
 * ====================================================================
 *
 * [엔티티(Entity)란?]
 * - 데이터베이스 테이블과 매핑되는 자바 클래스입니다
 * - JPA가 이 클래스를 보고 테이블을 생성하고, CRUD 작업을 수행합니다
 * - 하나의 엔티티 인스턴스 = 테이블의 한 행(Row)
 *
 * [현재 구조]
 * - 엔티티는 데이터(getter/setter)만 제공
 * - 모든 비즈니스 로직은 PaymentService에서 처리
 *
 * [@Entity 어노테이션]
 * - 이 클래스가 JPA 엔티티임을 나타냅니다
 *
 * [@Table 어노테이션]
 * - 매핑할 테이블 정보를 지정합니다
 */
@Entity
@Table(name = "payments")
public class Payment {

    /**
     * 결제 고유 식별자 (Primary Key)
     *
     * [@Id]
     * - 이 필드가 테이블의 기본 키(PK)임을 나타냅니다
     * - 엔티티를 구분하는 유일한 식별자입니다
     *
     * [@GeneratedValue]
     * - 기본 키 생성 전략을 지정합니다
     * - GenerationType.IDENTITY: DB의 AUTO_INCREMENT 사용
     *   - MySQL, H2, PostgreSQL 등에서 사용
     *   - INSERT 시 DB가 자동으로 ID 생성
     *
     * [다른 생성 전략]
     * - SEQUENCE: DB 시퀀스 사용 (Oracle, PostgreSQL)
     * - TABLE: 별도 키 생성 테이블 사용
     * - AUTO: DB에 맞게 자동 선택
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 원래 가격
     *
     * [@Column 어노테이션]
     * - 컬럼 상세 설정을 지정합니다
     * - nullable = false: NOT NULL 제약조건
     * - 생략 시 필드명이 컬럼명이 됩니다 (카멜케이스 → 스네이크케이스)
     *   예: originalPrice → original_price
     */
    @Column(nullable = false)
    private Double originalPrice;

    /**
     * 할인 적용 후 금액
     */
    @Column(nullable = false)
    private Double discountedAmount;

    /**
     * 세금 적용 후 최종 금액
     */
    @Column(nullable = false)
    private Double taxedAmount;

    /**
     * 국가 코드
     *
     * [length 속성]
     * - VARCHAR 길이 지정 (기본값: 255)
     * - 국가 코드는 2자리면 충분하므로 10으로 제한
     */
    @Column(nullable = false, length = 10)
    private String country;

    /**
     * VIP 고객 여부
     */
    @Column(nullable = false)
    private Boolean isVip;

    /**
     * 결제 상태
     *
     * [@Enumerated(EnumType.STRING)]
     * - Enum 타입을 DB에 저장하는 방식을 지정합니다
     * - EnumType.STRING: "COMPLETED" 문자열로 저장 (권장)
     * - EnumType.ORDINAL: 0, 1, 2... 숫자로 저장 (비권장)
     *
     * [왜 STRING을 권장하나요?]
     * - ORDINAL은 Enum 순서 변경 시 기존 데이터가 잘못 해석됩니다
     * - STRING은 순서 변경에 안전합니다
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    /**
     * 결제 생성 일시
     *
     * [LocalDateTime]
     * - Java 8+의 날짜/시간 API입니다
     * - 불변(immutable) 객체로 스레드 안전합니다
     * - 기존 Date, Calendar 대신 사용 권장
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 결제 수정 일시
     *
     * [updatable = false vs 없음]
     * - createdAt: updatable = false (생성 후 수정 불가)
     * - updatedAt: 수정 가능 (매번 갱신)
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 기본 생성자 (JPA 필수)
     *
     * [왜 기본 생성자가 필요한가요?]
     * - JPA가 DB에서 데이터를 읽어 객체를 생성할 때 사용합니다
     * - 리플렉션으로 객체를 생성한 후 필드를 채웁니다
     *
     * [protected vs public]
     * - protected: JPA만 사용하고 외부에서는 직접 호출 못하게
     * - 외부에서는 정적 팩토리 메서드나 Builder 패턴 사용 권장
     */
    protected Payment() {
    }

    /**
     * 결제 생성용 생성자
     *
     * [왜 private인가요?]
     * - 정적 팩토리 메서드(create)를 통해서만 객체 생성을 유도합니다
     * - 생성 시 필요한 로직(상태 초기화, 시간 설정)을 한 곳에서 관리
     */
    private Payment(Double originalPrice, Double discountedAmount, Double taxedAmount,
                    String country, Boolean isVip) {
        this.originalPrice = originalPrice;
        this.discountedAmount = discountedAmount;
        this.taxedAmount = taxedAmount;
        this.country = country;
        this.isVip = isVip;
        this.status = PaymentStatus.PENDING;  // 초기 상태는 대기
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 결제 생성 정적 팩토리 메서드
     *
     * [정적 팩토리 메서드 패턴]
     * - new 대신 의미 있는 이름의 메서드로 객체 생성
     * - 장점:
     *   1. 이름이 있어 가독성 좋음 (Payment.create vs new Payment)
     *   2. 호출할 때마다 새 객체를 만들지 않아도 됨 (캐싱 가능)
     *   3. 반환 타입의 하위 타입 반환 가능
     *   4. 생성 로직을 캡슐화
     *
     * @param originalPrice 원래 가격
     * @param discountedAmount 할인 후 금액
     * @param taxedAmount 세금 후 금액
     * @param country 국가 코드
     * @param isVip VIP 여부
     * @return 생성된 Payment 엔티티
     */
    public static Payment create(Double originalPrice, Double discountedAmount,
                                  Double taxedAmount, String country, Boolean isVip) {
        return new Payment(originalPrice, discountedAmount, taxedAmount, country, isVip);
    }

    // ==========================================================================
    // Getter / Setter 메서드들
    // ==========================================================================

    public Long getId() {
        return id;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public Double getDiscountedAmount() {
        return discountedAmount;
    }

    public Double getTaxedAmount() {
        return taxedAmount;
    }

    public String getCountry() {
        return country;
    }

    public Boolean getIsVip() {
        return isVip;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 결제 상태 변경
     * @param status 새로운 상태
     */
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    /**
     * 수정 일시 변경
     * @param updatedAt 수정 일시
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
