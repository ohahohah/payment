package com.example.payment_ddd.application.eventhandler;

import com.example.payment_ddd.domain.event.DomainEvent;

/**
 * DomainEventHandler - 도메인 이벤트 핸들러 인터페이스
 *
 * [Observer 패턴]
 * - 이벤트 발생 시 자동으로 통지받는 구조
 * - 발행자(Publisher)와 구독자(Subscriber)의 느슨한 결합
 *
 * [이벤트 기반 아키텍처]
 * - 이벤트 핸들러를 추가해도 기존 코드 수정 불필요
 * - 관심사 분리: 결제 완료 → 로깅, 정산, 알림 등 각각 독립적 처리
 */
public interface DomainEventHandler<T extends DomainEvent> {

    /**
     * 이벤트 처리
     *
     * @param event 도메인 이벤트
     */
    void handle(T event);

    /**
     * 지원하는 이벤트 타입 반환
     *
     * @return 이벤트 클래스
     */
    Class<T> supportedEventType();
}
