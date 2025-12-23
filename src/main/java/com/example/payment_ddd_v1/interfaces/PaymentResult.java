package com.example.payment_ddd_v1.interfaces;

import com.example.payment_ddd_v1.domain.model.Payment;
import com.example.payment_ddd_v1.domain.model.PaymentStatus;

/**
 * PaymentResult - 결제 결과 DTO
 *
 * [Interfaces 계층]
 * - 외부로 응답을 보내기 위한 DTO
 * - Domain 모델을 외부에 직접 노출하지 않음
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
        result.isVip = payment.isVip();
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
