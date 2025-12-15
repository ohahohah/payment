package com.example.payment.dto;

/**
 * ====================================================================
 * PaymentResult - 결제 결과 데이터 전송 객체 (DTO)
 * ====================================================================
 *
 * [이 클래스의 역할]
 * - 결제 처리 후의 결과 데이터를 담아서 전달합니다
 * - Service에서 Controller로, 또는 API 응답으로 전송됩니다
 *
 * [Record의 불변성 (Immutability)]
 * - Record의 모든 필드는 final입니다 (변경 불가)
 * - 한번 생성되면 값을 바꿀 수 없습니다
 * - 이로 인해 스레드 안전(Thread-safe)하고 예측 가능한 코드가 됩니다
 *
 * [Record 접근자 메서드]
 * - 기존 getter 메서드: getOriginalPrice()
 * - Record 접근자 메서드: originalPrice()  <- get 접두사 없음!
 *
 * 사용 예:
 * PaymentResult result = new PaymentResult(10000, 8500, 9350, "KR", true);
 * double original = result.originalPrice();  // 10000
 * double taxed = result.taxedAmount();       // 9350
 *
 * [스프링의 JSON 직렬화]
 * - 이 객체를 Controller에서 반환하면
 * - 스프링이 자동으로 JSON으로 변환합니다 (직렬화)
 *
 * 응답 JSON 예시:
 * {
 *   "originalPrice": 10000,
 *   "discountedAmount": 8500,
 *   "taxedAmount": 9350,
 *   "country": "KR",
 *   "isVip": true
 * }
 *
 * @param originalPrice 원래 가격
 * @param discountedAmount 할인 적용 후 금액
 * @param taxedAmount 세금 적용 후 최종 금액
 * @param country 국가 코드
 * @param isVip VIP 고객 여부
 */
public record PaymentResult(
        double originalPrice,
        double discountedAmount,
        double taxedAmount,
        String country,
        boolean isVip
) {
    // Record를 사용하면 아래 코드가 모두 자동 생성됩니다:
    //
    // 1. 생성자:
    //    public PaymentResult(double originalPrice, double discountedAmount,
    //                         double taxedAmount, String country, boolean isVip)
    //
    // 2. 접근자 메서드:
    //    public double originalPrice() { return this.originalPrice; }
    //    public double discountedAmount() { return this.discountedAmount; }
    //    public double taxedAmount() { return this.taxedAmount; }
    //    public String country() { return this.country; }
    //    public boolean isVip() { return this.isVip; }
    //
    // 3. equals(), hashCode(), toString() 메서드
}
