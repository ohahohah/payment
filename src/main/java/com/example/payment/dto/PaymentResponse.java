package com.example.payment.dto;

import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;

import java.time.LocalDateTime;

/**
 * ====================================================================
 * PaymentResponse - 결제 조회 응답 DTO
 * ====================================================================
 *
 * [이 DTO의 역할]
 * - 결제 조회 API의 응답 데이터
 * - Payment 엔티티의 정보를 클라이언트에게 전달
 * - 엔티티 직접 노출 대신 DTO 사용 (캡슐화)
 *
 * [엔티티 vs DTO]
 * - 엔티티: DB 테이블과 매핑, 내부 구조
 * - DTO: API 스펙, 외부 인터페이스
 * - 분리 이유: API 변경이 DB 구조에 영향 주지 않도록
 *
 * [정적 팩토리 메서드 from()]
 * - 엔티티 → DTO 변환을 담당합니다
 * - 변환 로직을 한 곳에서 관리할 수 있습니다
 * - 네이밍 관례: from(원본), of(여러 파라미터)
 *
 * @param id 결제 ID - DB 자동 생성 식별자
 * @param amt1 원래 가격 (Original Price)
 * @param amt2 할인 후 금액 (Discounted Amount)
 * @param amt3 세금 후 금액 (Taxed Amount)
 * @param cd 국가 코드 (Country Code)
 * @param flag VIP 여부 (isVip)
 * @param stat 결제 상태 (Status) - P, C, F, R
 * @param cdt 생성 일시 (Created DateTime)
 * @param udt 수정 일시 (Updated DateTime)
 */
public record PaymentResponse(
        Long id,
        Double amt1,        // 원래 가격
        Double amt2,        // 할인 후 금액
        Double amt3,        // 세금 후 금액
        String cd,          // 국가 코드
        Boolean flag,       // VIP 여부
        PaymentStatus stat, // 결제 상태
        LocalDateTime cdt,  // 생성 일시
        LocalDateTime udt   // 수정 일시
) {

    /**
     * [엔티티 → DTO 변환 메서드]
     *
     * [정적 팩토리 메서드 패턴]
     * - 생성자 대신 의미있는 이름의 메서드 사용
     * - from: 하나의 매개변수로부터 변환
     * - of: 여러 매개변수를 조합
     *
     * @param payment 변환할 Payment 엔티티
     * @return PaymentResponse DTO
     */
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getAmt1(),
                payment.getAmt2(),
                payment.getAmt3(),
                payment.getCd(),
                payment.getFlag(),
                payment.getStat(),
                payment.getCdt(),
                payment.getUdt()
        );
    }
}
