package com.example.payment_ddd_v1_1.domain.model;

/**
 * PaymentStatus - 결제 상태 (순수 Java)
 */
public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}
