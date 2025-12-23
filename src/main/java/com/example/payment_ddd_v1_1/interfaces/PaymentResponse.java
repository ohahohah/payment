package com.example.payment_ddd_v1_1.interfaces;

import com.example.payment_ddd_v1_1.domain.model.Payment;

import java.time.LocalDateTime;

/**
 * PaymentResponse - 결제 응답 DTO
 */
public class PaymentResponse {
    private Long id;
    private Double originalPrice;
    private Double discountedAmount;
    private Double taxedAmount;
    private String country;
    private Boolean isVip;
    private String status;
    private LocalDateTime createdAt;

    public static PaymentResponse from(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.id = payment.getId();
        response.originalPrice = payment.getOriginalPrice().getAmount();
        response.discountedAmount = payment.getDiscountedAmount().getAmount();
        response.taxedAmount = payment.getTaxedAmount().getAmount();
        response.country = payment.getCountry().getCode();
        response.isVip = payment.isVip();
        response.status = payment.getStatus().name();
        response.createdAt = payment.getCreatedAt();
        return response;
    }

    public Long getId() { return id; }
    public Double getOriginalPrice() { return originalPrice; }
    public Double getDiscountedAmount() { return discountedAmount; }
    public Double getTaxedAmount() { return taxedAmount; }
    public String getCountry() { return country; }
    public Boolean getIsVip() { return isVip; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
