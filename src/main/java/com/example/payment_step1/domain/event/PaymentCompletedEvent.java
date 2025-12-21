package com.example.payment_step1.domain.event;

import com.example.payment_step1.domain.model.Money;

import java.time.LocalDateTime;

/**
 * PaymentCompletedEvent - 결제 완료 이벤트
 *
 * [Record 사용]
 * - Java 16+의 Record는 불변 데이터 클래스에 적합
 * - equals, hashCode, toString 자동 생성
 * - getter 자동 생성 (paymentId(), finalAmount(), occurredAt())
 */
public record PaymentCompletedEvent(
        Long paymentId,
        Money finalAmount,
        LocalDateTime occurredAt
) implements DomainEvent {

    /**
     * 간편 생성자 - 발생 시간 자동 설정
     */
    public PaymentCompletedEvent(Long paymentId, Money finalAmount) {
        this(paymentId, finalAmount, LocalDateTime.now());
    }
}
