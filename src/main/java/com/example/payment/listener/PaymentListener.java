package com.example.payment.listener;

import com.example.payment.dto.PaymentResult;

/**
 * ====================================================================
 * PaymentListener - 결제 이벤트 리스너 인터페이스 (옵저버 패턴의 Observer)
 * ====================================================================
 *
 * [옵저버 패턴 (Observer Pattern)이란?]
 * - GoF 디자인 패턴 중 하나입니다
 * - 어떤 객체의 상태가 변할 때, 관련된 객체들에게 자동으로 알려주는 패턴입니다
 * - "발행-구독(Publish-Subscribe)" 패턴이라고도 합니다
 *
 * [옵저버 패턴의 구성 요소]
 * 1. Subject (주체/발행자) - PaymentProcessor
 *    - 상태 변화를 알리는 쪽입니다
 *    - 옵저버 목록을 관리하고, 이벤트 발생 시 알립니다
 *
 * 2. Observer (옵저버/구독자) - PaymentListener (이 인터페이스)
 *    - 알림을 받는 쪽의 인터페이스입니다
 *    - 공통된 알림 메서드를 정의합니다
 *
 * 3. ConcreteObserver (구체적 옵저버) - LoggingListener, SettlementListener
 *    - Observer 인터페이스의 구현체입니다
 *    - 알림을 받았을 때 실제로 수행할 동작을 정의합니다
 *
 * [옵저버 패턴의 장점]
 * 1. 느슨한 결합 (Loose Coupling)
 *    - Subject는 Observer의 구체적인 클래스를 몰라도 됩니다
 *    - 인터페이스만 알면 됩니다
 *
 * 2. 개방-폐쇄 원칙 (OCP)
 *    - 새로운 Observer 추가 시 기존 코드 수정이 필요 없습니다
 *    - 새 Listener 클래스만 만들면 됩니다
 *
 * 3. 일대다 관계
 *    - 하나의 이벤트(결제 완료)를 여러 Listener가 처리할 수 있습니다
 *
 * [스프링에서의 활용]
 * - 스프링은 ApplicationEvent, @EventListener 등 이벤트 시스템을 제공합니다
 * - 이 프로젝트에서는 직접 옵저버 패턴을 구현했습니다 (학습 목적)
 */
public interface PaymentListener {

    /**
     * 결제 완료 시 호출되는 콜백 메서드
     *
     * [콜백 (Callback)이란?]
     * - 특정 이벤트가 발생했을 때 호출되는 메서드입니다
     * - 이벤트 핸들러, 훅(Hook)이라고도 부릅니다
     * - PaymentProcessor가 결제 처리 후 이 메서드를 호출합니다
     *
     * [메서드 시그니처]
     * - 반환 타입: void (아무것도 반환하지 않음)
     * - 파라미터: PaymentResult (결제 결과 정보)
     *
     * @param result 결제 처리 결과 (원가, 할인가, 세금 적용가, 국가, VIP 여부)
     */
    void onPaymentCompleted(PaymentResult result);
}
