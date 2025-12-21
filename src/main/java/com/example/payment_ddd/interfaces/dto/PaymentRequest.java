package com.example.payment_ddd.interfaces.dto;

/**
 * PaymentRequest - 결제 요청 DTO
 *
 * [DTO(Data Transfer Object)]
 * - 외부 인터페이스(REST API)와 내부 도메인 사이의 데이터 전송
 * - 도메인 객체를 직접 노출하지 않음
 *
 * [왜 도메인 객체를 직접 노출하지 않나요?]
 * 1. API 스펙과 도메인 모델의 독립성 유지
 * 2. 클라이언트에 불필요한 내부 정보 노출 방지
 * 3. API 변경 시 도메인 영향 없음
 */
public record PaymentRequest(
        double amount,
        String country,
        boolean isVip
) {
}
