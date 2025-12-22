package com.example.payment_ddd_v1.interfaces;

import com.example.payment_ddd_v1.domain.model.Payment;

/**
 * PaymentDto - 요청/응답 DTO
 *
 * [Interfaces 계층]
 * - 외부와의 데이터 교환용 객체
 * - Domain 모델을 외부에 직접 노출하지 않음
 */
public class PaymentDto {

    /**
     * 결제 생성 요청
     */
    public record Request(
            double amount,
            String country,
            boolean isVip
    ) {}

    /**
     * 결제 응답
     */
    public record Response(
            Long id,
            double originalPrice,
            double discountedAmount,
            double taxedAmount,
            String country,
            boolean isVip,
            String status
    ) {
        public static Response from(Payment payment) {
            return new Response(
                    payment.getId(),
                    payment.getOriginalPrice().getAmount(),
                    payment.getDiscountedAmount().getAmount(),
                    payment.getTaxedAmount().getAmount(),
                    payment.getCountry().getCode(),
                    payment.isVip(),
                    payment.getStatus().name()
            );
        }
    }
}
