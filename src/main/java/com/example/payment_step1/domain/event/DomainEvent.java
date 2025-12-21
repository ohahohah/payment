package com.example.payment_step1.domain.event;

import java.time.LocalDateTime;

/**
 * DomainEvent - 도메인 이벤트 인터페이스
 *
 * ============================================================================
 * [도메인 이벤트란?]
 * ============================================================================
 *
 * 도메인에서 발생한 "과거의 사실"을 나타내는 객체입니다.
 *
 * 특징:
 * 1. 불변 - 이미 일어난 일이므로 변경 불가
 * 2. 과거형 이름 - PaymentCompleted (완료됨), OrderShipped (배송됨)
 * 3. 발생 시점 포함 - occurredAt
 *
 * ============================================================================
 * [왜 도메인 이벤트를 사용하나요?]
 * ============================================================================
 *
 * Anti-DDD (기존):
 *   public void completePayment() {
 *       payment.setStatus(COMPLETED);
 *       loggingService.log(...);        // 직접 호출
 *       settlementService.process(...); // 직접 호출
 *       notificationService.send(...);  // 직접 호출
 *   }
 *
 * DDD (개선):
 *   public void complete() {
 *       this.status = COMPLETED;
 *       registerEvent(new PaymentCompletedEvent(...));  // 이벤트만 등록
 *   }
 *   // 이벤트 핸들러가 로깅, 정산, 알림을 각각 처리
 *
 * 장점:
 * 1. 느슨한 결합 - 결제 로직이 로깅/정산을 몰라도 됨
 * 2. 확장 용이 - 새 기능 추가 시 핸들러만 추가
 * 3. 감사 추적 - 무슨 일이 언제 일어났는지 기록
 */
public interface DomainEvent {

    /**
     * 이벤트 발생 시간
     */
    LocalDateTime occurredAt();
}
