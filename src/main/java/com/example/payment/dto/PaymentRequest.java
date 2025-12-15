package com.example.payment.dto;

/**
 * ====================================================================
 * PaymentRequest - 결제 요청 데이터 전송 객체 (DTO)
 * ====================================================================
 *
 * [DTO (Data Transfer Object)란?]
 * - 계층 간 데이터 전송을 위한 객체입니다
 * - 주로 Controller와 Service 사이, 또는 외부 API와 데이터를 주고받을 때 사용합니다
 * - 비즈니스 로직 없이 순수하게 데이터만 담는 역할입니다
 *
 * [Java Record란?] (Java 14+, 정식 Java 16+)
 * - 불변(immutable) 데이터 클래스를 간결하게 정의하는 문법입니다
 * - record를 사용하면 다음이 자동 생성됩니다:
 *   1. private final 필드
 *   2. 모든 필드를 받는 생성자 (All-args constructor)
 *   3. 각 필드의 접근자 메서드 (originalPrice(), country(), isVip())
 *   4. equals(), hashCode(), toString() 메서드
 *
 * [기존 클래스로 작성했다면?]
 * 아래처럼 훨씬 긴 코드가 필요했을 것입니다:
 *
 * public class PaymentRequest {
 *     private final double originalPrice;
 *     private final String country;
 *     private final boolean isVip;
 *
 *     public PaymentRequest(double originalPrice, String country, boolean isVip) {
 *         this.originalPrice = originalPrice;
 *         this.country = country;
 *         this.isVip = isVip;
 *     }
 *
 *     public double getOriginalPrice() { return originalPrice; }
 *     public String getCountry() { return country; }
 *     public boolean isVip() { return isVip; }
 *
 *     // equals, hashCode, toString도 구현 필요...
 * }
 *
 * [스프링에서의 사용]
 * - @RequestBody 어노테이션과 함께 사용하면
 * - HTTP 요청의 JSON 본문이 자동으로 이 객체로 변환됩니다 (역직렬화)
 *
 * 예시 JSON 요청:
 * {
 *   "originalPrice": 10000,
 *   "country": "KR",
 *   "isVip": true
 * }
 *
 * @param originalPrice 원래 가격 (할인 적용 전)
 * @param country 국가 코드 (예: "KR", "US")
 * @param isVip VIP 고객 여부
 */
public record PaymentRequest(
        double originalPrice,
        String country,
        boolean isVip
) {
    // Record는 본문이 비어있어도 됩니다
    // 필요한 경우 추가 메서드나 유효성 검사를 여기에 작성할 수 있습니다

    // 예: 컴팩트 생성자로 유효성 검사 추가
    // public PaymentRequest {
    //     if (originalPrice < 0) {
    //         throw new IllegalArgumentException("가격은 0 이상이어야 합니다");
    //     }
    // }
}
