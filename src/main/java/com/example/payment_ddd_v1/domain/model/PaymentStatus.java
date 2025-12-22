package com.example.payment_ddd_v1.domain.model;

/**
 * PaymentStatus - 결제 상태
 */
public enum PaymentStatus {
    PENDING,    // 대기
    COMPLETED,  // 완료
    FAILED,     // 실패
    REFUNDED    // 환불
}
