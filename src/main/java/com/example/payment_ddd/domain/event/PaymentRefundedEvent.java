package com.example.payment_ddd.domain.event;

import com.example.payment_ddd.domain.model.Money;

import java.time.LocalDateTime;

/**
 * PaymentRefundedEvent - 결제 환불 도메인 이벤트
 */
public record PaymentRefundedEvent(
        Long paymentId,
        Money refundedAmount,
        LocalDateTime occurredAt
) implements DomainEvent {

    public PaymentRefundedEvent(Long paymentId, Money refundedAmount) {
        this(paymentId, refundedAmount, LocalDateTime.now());
    }
}
