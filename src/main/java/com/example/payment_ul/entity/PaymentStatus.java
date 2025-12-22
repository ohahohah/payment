package com.example.payment_ul.entity;

/**
 * ====================================================================
 * PaymentStatus - 결제 상태 Enum (유비쿼터스 랭귀지 적용)
 * ====================================================================
 *
 * [유비쿼터스 랭귀지 (Ubiquitous Language)]
 * - 도메인 전문가와 개발자가 공통으로 사용하는 언어입니다
 * - 코드, 문서, 대화에서 같은 용어를 사용합니다
 *
 * [변경 전 → 변경 후]
 * - P → PENDING (결제 대기)
 * - C → COMPLETED (결제 완료)
 * - F → FAILED (결제 실패)
 * - R → REFUNDED (환불 완료)
 */
public enum PaymentStatus {
    PENDING,    // 변경: P → PENDING
    COMPLETED,  // 변경: C → COMPLETED
    FAILED,     // 변경: F → FAILED
    REFUNDED    // 변경: R → REFUNDED
}
