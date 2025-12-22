package com.example.payment_ul.dto;

import com.example.payment_ul.entity.Payment;
import com.example.payment_ul.entity.PaymentStatus;

import java.time.LocalDateTime;

/**
 * PaymentResponse - 결제 조회 응답 DTO (유비쿼터스 랭귀지 적용)
 */
public record PaymentResponse(
        Long id,
        Double originalPrice,
        Double discountedAmount,
        Double taxedAmount,
        String country,
        Boolean isVip,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOriginalPrice(),
                payment.getDiscountedAmount(),
                payment.getTaxedAmount(),
                payment.getCountry(),
                payment.getIsVip(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
