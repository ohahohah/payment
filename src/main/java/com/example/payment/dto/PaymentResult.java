package com.example.payment.dto;

/**
 * ====================================================================
 * PaymentResult - 결제 처리 결과 DTO
 * ====================================================================
 *
 * [이 DTO의 역할]
 * - 결제 처리 후 클라이언트에게 반환하는 결과 데이터
 * - 할인/세금 적용 과정의 금액 변화를 보여줍니다
 *
 * [금액 흐름]
 * amt1 (원래 가격)
 *   ↓ 할인 적용 (VIP 15%, 일반 10%)
 * amt2 (할인 후 금액)
 *   ↓ 세금 적용 (한국 10%, 미국 7%)
 * amt3 (최종 결제 금액)
 *
 * [Record 사용 이유]
 * - 결제 결과는 생성 후 변경되지 않음 (불변)
 * - 간결한 코드로 DTO 정의 가능
 * - equals/hashCode 자동 구현
 *
 * @param amt1 원래 가격 (Original Price) - 할인 적용 전 금액
 * @param amt2 할인 후 금액 (Discounted Amount) - 할인 적용 후 금액
 * @param amt3 세금 후 금액 (Taxed Amount) - 최종 결제 금액
 * @param cd 국가 코드 (Country Code) - 세금 정책 결정에 사용
 * @param flag VIP 여부 (isVip) - 할인율 결정에 사용
 */
public record PaymentResult(
        double amt1,    // 원래 가격
        double amt2,    // 할인 후 금액
        double amt3,    // 세금 후 금액
        String cd,      // 국가 코드
        boolean flag    // VIP 여부
) {
}
