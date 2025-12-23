package com.example.payment_ddd_v1_1.interfaces;

/**
 * PaymentRequest - 결제 요청 DTO
 */
public class PaymentRequest {
    private Double amount;
    private String countryCode;
    private Boolean isVip;

    public PaymentRequest() {}

    public PaymentRequest(Double amount, String countryCode, Boolean isVip) {
        this.amount = amount;
        this.countryCode = countryCode;
        this.isVip = isVip;
    }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public Boolean getIsVip() { return isVip; }
    public void setIsVip(Boolean isVip) { this.isVip = isVip; }
}
