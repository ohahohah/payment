package com.example.payment_step1.domain.event;

import com.example.payment_step1.domain.model.Money;

import java.time.LocalDateTime;

/**
 * PaymentRefundedEvent - 결제 환불 이벤트
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
