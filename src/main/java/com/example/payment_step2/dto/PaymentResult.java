package com.example.payment_step2.dto;

/**
 * PaymentResult - 결제 처리 결과 DTO
 *
 * [외부 API용 DTO - primitive 타입 유지]
 *
 * Service에서 Money.getAmount(), Country.getCode()로
 * Value Object를 primitive 타입으로 변환하여 반환합니다.
 *
 * 금액 흐름: originalPrice -> discountedAmount -> taxedAmount
 */
public record PaymentResult(
        double originalPrice,
        double discountedAmount,
        double taxedAmount,
        String country,
        boolean isVip
) {
}
