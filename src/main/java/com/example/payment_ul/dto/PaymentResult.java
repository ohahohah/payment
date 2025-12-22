package com.example.payment_ul.dto;

/**
 * PaymentResult - 결제 처리 결과 DTO (유비쿼터스 랭귀지 적용)
 *
 * 금액 흐름: originalPrice → discountedAmount → taxedAmount
 */
public record PaymentResult(
        double originalPrice,
        double discountedAmount,
        double taxedAmount,
        String country,
        boolean isVip
) {
}
