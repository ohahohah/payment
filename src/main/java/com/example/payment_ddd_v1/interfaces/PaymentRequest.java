package com.example.payment_ddd_v1.interfaces;

/**
 * PaymentRequest - 결제 요청 DTO
 *
 * [Interfaces 계층]
 * - 외부 요청을 받기 위한 DTO
 * - Domain 모델과 분리
 */
public class PaymentRequest {
    private Double price;
    private String countryCode;
    private Boolean isVip;

    public PaymentRequest() {}

    public PaymentRequest(Double price, String countryCode, Boolean isVip) {
        this.price = price;
        this.countryCode = countryCode;
        this.isVip = isVip;
    }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public Boolean getIsVip() { return isVip; }
    public void setIsVip(Boolean isVip) { this.isVip = isVip; }
}
