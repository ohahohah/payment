package com.example.payment.entity;

/**
 * ====================================================================
 * PaymentStatus - 결제 상태 열거형 (Enum)
 * ====================================================================
 *
 * [Enum(열거형)이란?]
 * - 관련된 상수들을 묶어서 정의하는 특별한 클래스입니다
 * - 타입 안전성을 보장합니다 (잘못된 값 입력 방지)
 * - 가독성이 좋고 IDE 자동완성 지원
 *
 * [왜 String 대신 Enum을 사용하나요?]
 * - String: "PENDING", "pending", "Pending" 등 실수 가능
 * - Enum: PaymentStatus.PENDING 만 허용 (컴파일 타임 검증)
 *
 * [현업에서의 활용]
 * - 주문 상태: ORDERED, SHIPPING, DELIVERED, CANCELLED
 * - 결제 상태: PENDING, COMPLETED, FAILED, REFUNDED
 * - 회원 등급: BRONZE, SILVER, GOLD, VIP
 *
 * [JPA와 Enum]
 * - @Enumerated(EnumType.STRING): DB에 문자열로 저장 (권장)
 * - @Enumerated(EnumType.ORDINAL): DB에 순서(0,1,2...)로 저장 (비권장)
 *   - ORDINAL은 Enum 순서 변경 시 데이터가 꼬일 수 있습니다
 */
public enum PaymentStatus {

    /**
     * 결제 대기 중
     * - 결제 요청은 받았으나 아직 처리되지 않은 상태
     */
    PENDING("대기"),

    /**
     * 결제 완료
     * - 정상적으로 결제가 처리된 상태
     */
    COMPLETED("완료"),

    /**
     * 결제 실패
     * - 결제 처리 중 오류가 발생한 상태
     */
    FAILED("실패"),

    /**
     * 환불 완료
     * - 결제가 취소되어 환불된 상태
     */
    REFUNDED("환불");

    /**
     * 한글 설명
     * - Enum에 추가 정보를 담을 수 있습니다
     */
    private final String description;

    /**
     * Enum 생성자
     * - Enum 생성자는 항상 private입니다 (생략 가능)
     * - 외부에서 new PaymentStatus()로 생성할 수 없습니다
     *
     * @param description 상태 설명
     */
    PaymentStatus(String description) {
        this.description = description;
    }

    /**
     * 상태 설명 반환
     *
     * @return 한글 설명
     */
    public String getDescription() {
        return description;
    }
}
