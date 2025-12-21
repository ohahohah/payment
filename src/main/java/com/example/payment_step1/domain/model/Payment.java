package com.example.payment_step1.domain.model;

import com.example.payment_step1.domain.event.DomainEvent;
import com.example.payment_step1.domain.event.PaymentCompletedEvent;
import com.example.payment_step1.domain.event.PaymentRefundedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Payment - 결제 Aggregate Root (Rich Domain Model)
 *
 * ============================================================================
 * [Anti-DDD vs DDD 핵심 차이점]
 * ============================================================================
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ Anti-DDD (Anemic Domain Model)                                         │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ - Entity는 데이터만 보관 (getter/setter)                                │
 * │ - 비즈니스 로직은 Service에 위치                                        │
 * │ - 상태 변경을 아무데서나 가능: payment.setStatus(COMPLETED)              │
 * │ - 도메인 규칙이 코드 여기저기에 흩어짐                                   │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ DDD (Rich Domain Model)                                                │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ - Entity가 데이터 + 비즈니스 로직 모두 보유                             │
 * │ - 상태 변경은 의미있는 메서드로만: payment.complete()                   │
 * │ - 도메인 규칙이 Entity 안에 캡슐화                                      │
 * │ - setter 없음! (불변성 보장)                                            │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ============================================================================
 * [코드 비교]
 * ============================================================================
 *
 * Anti-DDD (PaymentService에서):
 *   public void completePayment(Payment payment) {
 *       if (payment.getStatus() != PaymentStatus.PENDING) {
 *           throw new IllegalStateException(...);
 *       }
 *       payment.setStatus(PaymentStatus.COMPLETED);
 *       payment.setUpdatedAt(LocalDateTime.now());
 *       // 로깅, 정산 서비스 직접 호출...
 *   }
 *
 * DDD (Payment 안에서):
 *   public void complete() {
 *       if (this.status != PaymentStatus.PENDING) {
 *           throw new IllegalStateException(...);
 *       }
 *       this.status = PaymentStatus.COMPLETED;
 *       this.updatedAt = LocalDateTime.now();
 *       registerEvent(new PaymentCompletedEvent(...));
 *   }
 *
 * ============================================================================
 * [Aggregate Root란?]
 * ============================================================================
 *
 * - 관련된 도메인 객체들의 묶음(클러스터)의 진입점
 * - 외부에서는 반드시 Aggregate Root를 통해서만 내부 객체에 접근
 * - 트랜잭션 일관성의 경계
 * - 이 예제에서 Payment가 Aggregate Root
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

    // 도메인 이벤트 저장소
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // ==========================================================================
    // 생성자 - private (팩토리 메서드 사용)
    // ==========================================================================

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

    // ==========================================================================
    // 팩토리 메서드
    // ==========================================================================

    /**
     * 결제 생성 팩토리 메서드
     *
     * [왜 팩토리 메서드를 사용하나요?]
     * - 의미 있는 이름: Payment.create(...) vs new Payment(...)
     * - 유효성 검증 로직 포함 가능
     * - 생성 의도를 명확히 표현
     */
    public static Payment create(Money originalPrice, Money discountedAmount,
                                  Money taxedAmount, Country country, boolean isVip) {
        return new Payment(originalPrice, discountedAmount, taxedAmount, country, isVip);
    }

    /**
     * DB에서 복원용 팩토리 메서드 (Repository에서 사용)
     */
    public static Payment reconstitute(Long id, Money originalPrice, Money discountedAmount,
                                        Money taxedAmount, Country country, boolean isVip,
                                        PaymentStatus status, LocalDateTime createdAt,
                                        LocalDateTime updatedAt) {
        Payment payment = new Payment(originalPrice, discountedAmount, taxedAmount, country, isVip);
        payment.id = id;
        payment.status = status;
        return payment;
    }

    // ==========================================================================
    // 비즈니스 메서드 - 상태 변경은 여기서만!
    // ==========================================================================

    /**
     * 결제 완료 처리
     *
     * [Rich Domain Model의 핵심]
     * - 상태 변경 규칙이 엔티티 안에 캡슐화
     * - "대기 상태에서만 완료 가능" 규칙이 여기에!
     * - 도메인 이벤트 자동 등록
     *
     * [Anti-DDD와의 차이]
     * - Anti-DDD: Service에서 payment.setStatus(COMPLETED) 호출
     * - DDD: payment.complete() 호출 → 규칙 검증 + 상태 변경 + 이벤트 등록
     */
    public void complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "대기 상태의 결제만 완료할 수 있습니다. 현재 상태: " + this.status);
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
            throw new IllegalStateException(
                    "대기 상태의 결제만 실패 처리할 수 있습니다. 현재 상태: " + this.status);
        }

        this.status = PaymentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 환불 처리
     *
     * [도메인 규칙 캡슐화]
     * - "완료된 결제만 환불 가능" 규칙이 여기에!
     * - Service가 아닌 Entity가 규칙을 알고 있음
     */
    public void refund() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "완료된 결제만 환불할 수 있습니다. 현재 상태: " + this.status);
        }

        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();

        // 도메인 이벤트 등록
        registerEvent(new PaymentRefundedEvent(this.id, this.taxedAmount));
    }

    // ==========================================================================
    // 도메인 이벤트 관리
    // ==========================================================================

    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * 도메인 이벤트 반환 및 초기화
     * Application Service에서 호출하여 이벤트 발행
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
     * ID는 한 번만 할당 가능
     */
    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("ID는 한 번만 할당할 수 있습니다");
        }
        this.id = id;
    }
}
