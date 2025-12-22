package com.example.payment.observer;

import com.example.payment.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * LoggingObserver - 결제 로깅 옵저버
 */
@Component
public class LoggingObserver implements PaymentObserver {

    private static final Logger log = LoggerFactory.getLogger(LoggingObserver.class);

    @Override
    public void onPaymentCompleted(PaymentResult result) {
        log.info("[LOG] payment completed: {}", result.amt3());
    }
}
