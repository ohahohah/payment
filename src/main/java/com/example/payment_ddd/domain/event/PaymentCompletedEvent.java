package com.example.payment_ddd.domain.event;

import com.example.payment_ddd.domain.model.Money;

import java.time.LocalDateTime;

/**
 * PaymentCompletedEvent - 결제 완료 도메인 이벤트
 *
 * [Record 사용]
 * - 불변 데이터 전송 객체에 적합
 * - equals, hashCode, toString 자동 생성
 */
public record PaymentCompletedEvent(
        Long paymentId,
        Money finalAmount,
        LocalDateTime occurredAt
) implements DomainEvent {

    public PaymentCompletedEvent(Long paymentId, Money finalAmount) {
        this(paymentId, finalAmount, LocalDateTime.now());
    }
}
