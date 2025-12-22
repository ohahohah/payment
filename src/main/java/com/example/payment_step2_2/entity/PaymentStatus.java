package com.example.payment_step2_2.entity;

/**
 * PaymentStatus - 결제 상태 열거형
 *
 * [상태 전이 규칙 - Payment Aggregate에서 검증]
 * PENDING -> COMPLETED (complete() 호출)
 * PENDING -> FAILED (fail() 호출)
 * COMPLETED -> REFUNDED (refund() 호출)
 */
public enum PaymentStatus {
    PENDING,    // 대기 중
    COMPLETED,  // 완료
    FAILED,     // 실패
    REFUNDED    // 환불됨
}
