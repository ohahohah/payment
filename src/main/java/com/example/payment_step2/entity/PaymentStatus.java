package com.example.payment_step2.entity;

/**
 * PaymentStatus - 결제 상태 열거형
 *
 * [상태 전이 규칙]
 * PENDING -> COMPLETED (complete() 호출)
 * COMPLETED -> REFUNDED (refund() 호출)
 *
 * 잘못된 전이는 Payment 엔티티에서 예외 발생
 */
public enum PaymentStatus {
    PENDING,    // 대기 중
    COMPLETED,  // 완료
    REFUNDED    // 환불됨
}
