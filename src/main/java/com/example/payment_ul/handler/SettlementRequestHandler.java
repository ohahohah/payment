package com.example.payment_ul.handler;

import com.example.payment_ul.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SettlementRequestHandler - 정산 요청 처리기
 *
 * 결제 완료 시 정산 요청을 처리합니다.
 * 10만원 이상 결제 시 즉시 정산 요청을 발송합니다.
 */
@Component
public class SettlementRequestHandler implements PaymentCompletionHandler {

    private static final Logger log = LoggerFactory.getLogger(SettlementRequestHandler.class);
    private static final double SETTLEMENT_THRESHOLD = 100000;

    @Override
    public void onPaymentCompleted(PaymentResult result) {
        if (result.taxedAmount() > SETTLEMENT_THRESHOLD) {
            log.info("[SETTLEMENT] 정산 요청 발송 - 결제 금액: {}", result.taxedAmount());
        }
    }
}
