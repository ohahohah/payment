package com.example.payment_step2.handler;

import com.example.payment_step2.dto.PaymentResult;

/**
 * PaymentCompletionHandler - 결제 완료 핸들러 인터페이스
 *
 * 결제 완료 시 실행할 후처리 로직을 정의합니다.
 */
public interface PaymentCompletionHandler {

    void onPaymentCompleted(PaymentResult result);
}
