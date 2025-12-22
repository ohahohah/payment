package com.example.payment.observer;

import com.example.payment.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SettlementObserver - 정산 옵저버
 */
@Component
public class SettlementObserver implements PaymentObserver {

    private static final Logger log = LoggerFactory.getLogger(SettlementObserver.class);

    @Override
    public void onPaymentCompleted(PaymentResult result) {
        if (result.amt3() > 100000) {
            log.info("정산 요청 발송");
        }
    }
}
