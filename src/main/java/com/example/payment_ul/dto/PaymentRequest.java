package com.example.payment_ul.dto;

/**
 * PaymentRequest - 결제 요청 DTO (유비쿼터스 랭귀지 적용)
 *
 * [JSON 예시]
 * { "originalPrice": 10000, "country": "KR", "isVip": true }
 */
public record PaymentRequest(
        double originalPrice,
        String country,
        boolean isVip
) {
}
