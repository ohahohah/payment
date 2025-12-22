package com.example.payment_step2_2.dto;

import com.example.payment_step2_2.entity.Payment;
import com.example.payment_step2_2.entity.PaymentStatus;

/**
 * PaymentResult - 결제 결과 DTO
 *
 * [payment_step1과 동일]
 */
public class PaymentResult {
    private Long id;
    private Double originalPrice;
    private Double discountedAmount;
    private Double taxedAmount;
    private String countryCode;
    private Boolean isVip;
    private PaymentStatus status;

    public static PaymentResult from(Payment payment) {
        PaymentResult result = new PaymentResult();
        result.id = payment.getId();
        result.originalPrice = payment.getOriginalPrice().getAmount();
        result.discountedAmount = payment.getDiscountedAmount().getAmount();
        result.taxedAmount = payment.getTaxedAmount().getAmount();
        result.countryCode = payment.getCountry().getCode();
        result.isVip = payment.getIsVip();
        result.status = payment.getStatus();
        return result;
    }

    public Long getId() { return id; }
    public Double getOriginalPrice() { return originalPrice; }
    public Double getDiscountedAmount() { return discountedAmount; }
    public Double getTaxedAmount() { return taxedAmount; }
    public String getCountryCode() { return countryCode; }
    public Boolean getIsVip() { return isVip; }
    public PaymentStatus getStatus() { return status; }
}
