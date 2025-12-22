package com.example.payment_step2.handler;

import com.example.payment_step2.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * PaymentAuditLogger - 결제 감사 로그 핸들러
 *
 * 결제 완료 시 감사 로그를 기록합니다.
 */
@Component
public class PaymentAuditLogger implements PaymentCompletionHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentAuditLogger.class);

    @Override
    public void onPaymentCompleted(PaymentResult result) {
        log.info("[감사로그] 결제 완료 - 금액: {}, 국가: {}, VIP: {}",
                result.taxedAmount(), result.country(), result.isVip());
    }
}
