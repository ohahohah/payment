package com.example.payment_step2.handler;

import com.example.payment_step2.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SettlementRequestHandler - 정산 요청 핸들러
 *
 * 결제 완료 시 정산 시스템에 요청을 보냅니다.
 */
@Component
public class SettlementRequestHandler implements PaymentCompletionHandler {

    private static final Logger log = LoggerFactory.getLogger(SettlementRequestHandler.class);

    @Override
    public void onPaymentCompleted(PaymentResult result) {
        log.info("[정산요청] 정산 시스템 호출 - 금액: {}", result.taxedAmount());
    }
}
