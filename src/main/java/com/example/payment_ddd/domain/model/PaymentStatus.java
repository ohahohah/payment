package com.example.payment_ddd.domain.model;

/**
 * 결제 상태 Enum
 */
public enum PaymentStatus {
    PENDING,    // 대기중
    COMPLETED,  // 완료
    FAILED,     // 실패
    REFUNDED    // 환불됨
}
