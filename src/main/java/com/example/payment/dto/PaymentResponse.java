package com.example.payment.dto;

import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;

import java.time.LocalDateTime;

/**
 * ====================================================================
 * PaymentResponse - 결제 응답 DTO
 * ====================================================================
 *
 * [응답 DTO의 역할]
 * - 클라이언트에게 반환할 데이터를 담는 객체입니다
 * - 엔티티를 직접 반환하지 않고 DTO로 변환하여 반환합니다
 *
 * [왜 엔티티를 직접 반환하지 않나요?]
 * 1. 캡슐화: 엔티티의 내부 구조가 API에 노출되지 않음
 * 2. 유연성: 엔티티 변경이 API 스펙에 영향을 주지 않음
 * 3. 보안: 민감한 필드(비밀번호 등)를 제외하고 필요한 것만 노출
 * 4. 순환 참조 방지: JPA 연관 관계로 인한 무한 루프 방지
 * 5. 버전 관리: API 버전별로 다른 응답 형식 지원 가능
 *
 * [Record로 DTO 정의]
 * - 불변 객체로 스레드 안전
 * - 간결한 코드
 * - JSON 직렬화/역직렬화 자동 지원
 *
 * @param id 결제 ID
 * @param originalPrice 원래 가격
 * @param discountedAmount 할인 후 금액
 * @param taxedAmount 세금 포함 최종 금액
 * @param country 국가 코드
 * @param isVip VIP 여부
 * @param status 결제 상태
 * @param statusDescription 상태 설명 (한글)
 * @param createdAt 생성 일시
 * @param updatedAt 수정 일시
 */
public record PaymentResponse(
        Long id,
        Double originalPrice,
        Double discountedAmount,
        Double taxedAmount,
        String country,
        Boolean isVip,
        PaymentStatus status,
        String statusDescription,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /**
     * 엔티티 → DTO 변환 정적 팩토리 메서드
     *
     * [정적 팩토리 메서드 패턴]
     * - from: 다른 타입에서 변환하여 생성할 때 사용하는 관례적 이름
     * - of: 여러 파라미터로 생성할 때
     * - valueOf: 기본 타입을 객체로 변환할 때
     *
     * [변환 로직을 DTO에 두는 이유]
     * - 변환 로직이 한 곳에 집중됨 (응집도 향상)
     * - 테스트하기 쉬움
     * - Controller가 단순해짐
     *
     * [메서드 참조로 사용 가능]
     * payments.stream()
     *         .map(PaymentResponse::from)  // 이렇게 사용
     *         .toList();
     *
     * @param payment 변환할 Payment 엔티티
     * @return 변환된 PaymentResponse DTO
     */
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOriginalPrice(),
                payment.getDiscountedAmount(),
                payment.getTaxedAmount(),
                payment.getCountry(),
                payment.getIsVip(),
                payment.getStatus(),
                payment.getStatus().getDescription(),  // Enum의 description 필드 활용
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
