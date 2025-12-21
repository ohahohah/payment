package com.example.payment_step1.domain.model;

/**
 * PaymentStatus - 결제 상태 열거형
 *
 * [변경 없음]
 * - Anti-DDD 버전과 동일
 * - 단순 상태 값이므로 변경 불필요
 */
public enum PaymentStatus {
    PENDING,    // 대기
    COMPLETED,  // 완료
    FAILED,     // 실패
    REFUNDED    // 환불
}
