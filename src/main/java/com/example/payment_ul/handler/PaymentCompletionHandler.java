package com.example.payment_ul.handler;

import com.example.payment_ul.dto.PaymentResult;

/**
 * PaymentCompletionHandler - 결제 완료 처리기 인터페이스
 *
 * 결제가 완료되었을 때 후속 처리를 담당하는 핸들러입니다.
 */
public interface PaymentCompletionHandler {

    /**
     * 결제 완료 시 호출되는 콜백 메서드
     *
     * @param result 결제 처리 결과
     */
    void onPaymentCompleted(PaymentResult result);
}
