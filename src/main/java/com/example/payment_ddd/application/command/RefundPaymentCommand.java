package com.example.payment_ddd.application.command;

/**
 * RefundPaymentCommand - 결제 환불 커맨드
 *
 * [Command 패턴]
 * - 환불 요청에 필요한 정보를 캡슐화
 * - 불변 객체 (Record 사용)
 */
public record RefundPaymentCommand(
        Long paymentId
) {
    public RefundPaymentCommand {
        if (paymentId == null) {
            throw new IllegalArgumentException("결제 ID는 필수입니다");
        }
    }
}
