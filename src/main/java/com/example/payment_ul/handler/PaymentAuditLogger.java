package com.example.payment_ul.handler;

import com.example.payment_ul.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * PaymentAuditLogger - 결제 감사 로그 기록기
 *
 * 결제 완료 시 감사 추적을 위한 로그를 기록합니다.
 */
@Component
public class PaymentAuditLogger implements PaymentCompletionHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentAuditLogger.class);

    @Override
    public void onPaymentCompleted(PaymentResult result) {
        log.info("[AUDIT] 결제 완료 - 최종 결제 금액: {}", result.taxedAmount());
    }
}
