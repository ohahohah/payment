package com.example.payment_ddd.interfaces.dto;

import com.example.payment_ddd.domain.model.Payment;

import java.time.LocalDateTime;

/**
 * PaymentResponse - 결제 응답 DTO
 *
 * [DTO 사용 이유]
 * - 도메인 객체(Payment)의 내부 구조를 숨김
 * - API 응답 형식을 자유롭게 설계 가능
 * - Value Object(Money)도 원시값으로 변환
 */
public record PaymentResponse(
        Long id,
        double originalPrice,
        double discountedAmount,
        double finalAmount,
        String country,
        boolean isVip,
        String status,
        LocalDateTime createdAt
) {
    /**
     * 도메인 객체 → DTO 변환
     *
     * [변환 책임]
     * - Interfaces 레이어에서 담당
     * - 도메인 객체는 DTO를 모름 (단방향 의존)
     */
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOriginalPrice().getAmount(),
                payment.getDiscountedAmount().getAmount(),
                payment.getTaxedAmount().getAmount(),
                payment.getCountry().getCode(),
                payment.isVip(),
                payment.getStatus().name(),
                payment.getCreatedAt()
        );
    }
}
