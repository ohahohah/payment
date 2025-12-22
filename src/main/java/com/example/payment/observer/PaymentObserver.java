package com.example.payment.observer;

import com.example.payment.dto.PaymentResult;

/**
 * PaymentObserver - 결제 이벤트 옵저버 인터페이스
 */
public interface PaymentObserver {

    void onPaymentCompleted(PaymentResult result);
}
