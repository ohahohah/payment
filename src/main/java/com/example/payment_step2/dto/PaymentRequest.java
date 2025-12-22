package com.example.payment_step2.dto;

/**
 * PaymentRequest - 결제 요청 DTO
 *
 * [외부 API용 DTO - primitive 타입 유지]
 *
 * DTO는 외부 시스템(클라이언트)과의 데이터 교환용이므로
 * JSON 직렬화가 쉬운 primitive 타입을 사용합니다.
 *
 * Service 계층에서 Money.of(), Country.of()로 변환 시
 * 자동으로 유효성 검증이 수행됩니다.
 */
public record PaymentRequest(
        double originalPrice,
        String country,
        boolean isVip
) {
}
