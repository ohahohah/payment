package com.example.payment_ddd.application.eventhandler;

import com.example.payment_ddd.domain.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SettlementEventHandler - 결제 완료 정산 핸들러
 *
 * [단일 책임 원칙(SRP)]
 * - 정산이라는 하나의 관심사만 처리
 *
 * [확장성]
 * - 새로운 핸들러 추가 시 기존 코드 수정 불필요
 * - 예: 알림 핸들러, 포인트 적립 핸들러 등
 */
public class SettlementEventHandler implements DomainEventHandler<PaymentCompletedEvent> {

    private static final Logger log = LoggerFactory.getLogger(SettlementEventHandler.class);

    @Override
    public void handle(PaymentCompletedEvent event) {
        log.info("[DDD-정산] 정산 처리 시작 - 결제 ID: {}, 정산 금액: {}",
                event.paymentId(),
                event.finalAmount());

        // 실제로는 정산 서비스 호출 또는 정산 테이블에 기록
        // settlementService.process(event.paymentId(), event.finalAmount());

        log.info("[DDD-정산] 정산 처리 완료 - 결제 ID: {}", event.paymentId());
    }

    @Override
    public Class<PaymentCompletedEvent> supportedEventType() {
        return PaymentCompletedEvent.class;
    }
}
