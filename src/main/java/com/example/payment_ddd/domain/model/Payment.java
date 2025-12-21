package com.example.payment_ddd.domain.model;

import com.example.payment_ddd.domain.event.DomainEvent;
import com.example.payment_ddd.domain.event.PaymentCompletedEvent;
import com.example.payment_ddd.domain.event.PaymentRefundedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Payment - 결제 Aggregate Root
 *
 * [Aggregate Root란?]
 * - 관련된 도메인 객체들의 클러스터(묶음)의 진입점
 * - 외부에서는 반드시 Aggregate Root를 통해서만 내부 객체에 접근
 * - 트랜잭션 일관성의 경계
 *
 * [Rich Domain Model]
 * - 비즈니스 로직이 엔티티 내부에 위치
 * - 상태 변경은 반드시 메서드를 통해서만 가능
 * - 도메인 규칙을 캡슐화
 *
 * [Anti-DDD(Anemic Model)와의 차이]
 * - Anti-DDD: setter로 아무 곳에서나 상태 변경 가능
 * - DDD: complete(), refund() 메서드로만 상태 변경, 규칙 검증 포함
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

    // 도메인 이벤트 저장 (나중에 발행)
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * 생성자 - private (팩토리 메서드 사용)
     */
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
     * 결제 생성 팩토리 메서드
     *
     * [팩토리 메서드 패턴]
     * - 복잡한 생성 로직을 캡슐화
     * - 유효성 검증 포함
     * - 의미 있는 이름으로 생성 의도 표현
     */
    public static Payment create(Money originalPrice, Money discountedAmount,
                                  Money taxedAmount, Country country, boolean isVip) {
        // 유효성 검증은 Value Object에서 이미 수행됨
        return new Payment(originalPrice, discountedAmount, taxedAmount, country, isVip);
    }

    /**
     * 영속성 복원용 팩토리 메서드 (Repository에서 사용)
     */
    public static Payment reconstitute(Long id, Money originalPrice, Money discountedAmount,
                                        Money taxedAmount, Country country, boolean isVip,
                                        PaymentStatus status, LocalDateTime createdAt,
                                        LocalDateTime updatedAt) {
        Payment payment = new Payment(originalPrice, discountedAmount, taxedAmount, country, isVip);
        payment.id = id;
        payment.status = status;
        // createdAt, updatedAt은 private final이라 직접 설정 불가
        // 실제로는 별도 생성자나 빌더 패턴 사용
        return payment;
    }

    /**
     * 결제 완료 처리
     *
     * [Rich Domain Model의 핵심]
     * - 상태 변경과 비즈니스 규칙이 엔티티 안에 캡슐화
     * - 도메인 이벤트 발행
     */
    public void complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 결제만 완료할 수 있습니다. 현재 상태: " + this.status);
        }

        this.status = PaymentStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();

        // 도메인 이벤트 등록
        registerEvent(new PaymentCompletedEvent(this.id, this.taxedAmount));
    }

    /**
     * 결제 실패 처리
     */
    public void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 결제만 실패 처리할 수 있습니다. 현재 상태: " + this.status);
        }

        this.status = PaymentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 환불 처리
     *
     * [도메인 규칙 캡슐화]
     * - "완료된 결제만 환불 가능" 규칙이 엔티티 안에 있음
     * - 서비스가 아닌 엔티티가 규칙을 알고 있음
     */
    public void refund() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 결제만 환불할 수 있습니다. 현재 상태: " + this.status);
        }

        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();

        // 도메인 이벤트 등록
        registerEvent(new PaymentRefundedEvent(this.id, this.taxedAmount));
    }

    /**
     * 도메인 이벤트 등록
     */
    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * 도메인 이벤트 반환 및 초기화
     * (Application Service에서 호출하여 이벤트 발행)
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }

    /**
     * 현재 등록된 이벤트 조회 (읽기 전용)
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    // ==========================================================================
    // Getter 메서드들 (Setter는 없음!)
    // ==========================================================================

    public Long getId() {
        return id;
    }

    public Money getOriginalPrice() {
        return originalPrice;
    }

    public Money getDiscountedAmount() {
        return discountedAmount;
    }

    public Money getTaxedAmount() {
        return taxedAmount;
    }

    public Country getCountry() {
        return country;
    }

    public boolean isVip() {
        return vip;
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
     * ID 설정 (저장 후 Repository에서 호출)
     */
    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("ID는 한 번만 할당할 수 있습니다");
        }
        this.id = id;
    }
}
