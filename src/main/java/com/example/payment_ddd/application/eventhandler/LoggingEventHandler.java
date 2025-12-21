package com.example.payment_ddd.application.eventhandler;

import com.example.payment_ddd.domain.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoggingEventHandler - 결제 완료 로깅 핸들러
 *
 * [단일 책임 원칙(SRP)]
 * - 로깅이라는 하나의 관심사만 처리
 * - 다른 핸들러(정산, 알림 등)와 독립적
 *
 * [Observer 패턴 구현]
 * - DomainEventHandler 인터페이스 구현
 * - PaymentCompletedEvent 발생 시 자동 호출
 */
public class LoggingEventHandler implements DomainEventHandler<PaymentCompletedEvent> {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventHandler.class);

    @Override
    public void handle(PaymentCompletedEvent event) {
        log.info("[DDD-로깅] 결제 완료 - ID: {}, 금액: {}, 시간: {}",
                event.paymentId(),
                event.finalAmount(),
                event.occurredAt());
    }

    @Override
    public Class<PaymentCompletedEvent> supportedEventType() {
        return PaymentCompletedEvent.class;
    }
}
