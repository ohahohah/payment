package com.example.payment_ddd.application.eventhandler;

import com.example.payment_ddd.domain.event.PaymentRefundedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RefundLoggingEventHandler - 환불 로깅 핸들러
 *
 * [Observer 패턴]
 * - PaymentRefundedEvent 구독
 * - 환불 발생 시 자동으로 로깅
 */
public class RefundLoggingEventHandler implements DomainEventHandler<PaymentRefundedEvent> {

    private static final Logger log = LoggerFactory.getLogger(RefundLoggingEventHandler.class);

    @Override
    public void handle(PaymentRefundedEvent event) {
        log.info("[DDD-환불 로깅] 환불 완료 - ID: {}, 환불 금액: {}, 시간: {}",
                event.paymentId(),
                event.refundedAmount(),
                event.occurredAt());
    }

    @Override
    public Class<PaymentRefundedEvent> supportedEventType() {
        return PaymentRefundedEvent.class;
    }
}
