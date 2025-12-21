package com.example.payment_ddd.domain.event;

import java.time.LocalDateTime;

/**
 * DomainEvent - 도메인 이벤트 인터페이스
 *
 * [도메인 이벤트란?]
 * - 도메인에서 발생한 "과거의 사실"을 나타냄
 * - 불변이며, 이미 일어난 일이므로 취소할 수 없음
 * - 이름은 과거형으로 작성 (PaymentCompleted, OrderShipped)
 *
 * [왜 도메인 이벤트를 사용하나요?]
 * 1. 느슨한 결합: 결제 완료 → 로깅, 정산 등을 분리
 * 2. 감사 추적: 무슨 일이 언제 일어났는지 기록
 * 3. 이벤트 소싱: 이벤트를 재생하여 상태 복원 가능
 * 4. 비동기 처리: 이벤트 기반 아키텍처 지원
 */
public interface DomainEvent {

    /**
     * 이벤트 발생 시간
     */
    LocalDateTime occurredAt();
}
