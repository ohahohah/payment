package com.example.payment.dto;

/**
 * ====================================================================
 * PaymentRequest - 결제 요청 DTO (Data Transfer Object)
 * ====================================================================
 *
 * [DTO란?]
 * - 계층 간 데이터 전송을 위한 객체입니다
 * - Controller ↔ Service ↔ 외부 시스템 간 데이터 교환에 사용
 * - 엔티티와 분리하여 API 스펙을 독립적으로 관리
 *
 * [Record (Java 16+)]
 * - 불변(Immutable) 데이터 클래스를 간결하게 정의
 * - 자동 생성: 생성자, getter, equals(), hashCode(), toString()
 * - 필드 접근: record.필드명() (getter 대신)
 *
 * [Record vs Class]
 * - Record: 불변, 간결, DTO에 적합
 * - Class: 변경 가능, 상속 가능, 엔티티에 적합
 *
 * [JSON 직렬화]
 * - Spring Boot는 Jackson 라이브러리로 JSON ↔ 객체 변환
 * - Record의 컴포넌트(필드)명이 JSON 키가 됨
 * - 예: { "amt1": 10000, "cd": "KR", "flag": true }
 *
 * @param amt1 원래 가격 (Original Price) - 할인 적용 전 금액
 * @param cd 국가 코드 (Country Code) - "KR", "US" 등
 * @param flag VIP 고객 여부 (isVip) - true면 VIP 할인 적용
 */
public record PaymentRequest(
        double amt1,    // 원래 가격
        String cd,      // 국가 코드
        boolean flag    // VIP 여부
) {
}
