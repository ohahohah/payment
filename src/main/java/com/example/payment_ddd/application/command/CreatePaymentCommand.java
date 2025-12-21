package com.example.payment_ddd.application.command;

/**
 * CreatePaymentCommand - 결제 생성 커맨드
 *
 * [Command 패턴]
 * - 작업 요청을 객체로 캡슐화
 * - 작업에 필요한 모든 정보를 포함
 * - 불변 객체로 설계
 *
 * [CQRS에서의 Command]
 * - 상태를 변경하는 의도를 표현
 * - Application Service의 입력으로 사용
 * - Controller → Command → Application Service 흐름
 */
public record CreatePaymentCommand(
        double amount,
        String country,
        boolean isVip
) {
    public CreatePaymentCommand {
        if (amount <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("국가 코드는 필수입니다");
        }
    }
}
