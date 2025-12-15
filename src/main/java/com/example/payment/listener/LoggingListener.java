package com.example.payment.listener;

import com.example.payment.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ====================================================================
 * LoggingListener - 결제 로깅 리스너 (옵저버 패턴의 ConcreteObserver)
 * ====================================================================
 *
 * [이 클래스의 역할]
 * - 결제가 완료되면 로그를 기록합니다
 * - PaymentListener 인터페이스의 구현체입니다
 *
 * [로깅 (Logging)의 중요성]
 * 1. 디버깅: 문제 발생 시 원인 추적
 * 2. 모니터링: 시스템 상태 파악
 * 3. 감사 추적 (Audit Trail): 언제 어떤 거래가 있었는지 기록
 * 4. 분석: 사용 패턴, 성능 분석
 *
 * [SLF4J와 Logback]
 * - SLF4J: Simple Logging Facade for Java (로깅 추상화 인터페이스)
 * - Logback: SLF4J의 구현체 중 하나 (Spring Boot 기본)
 *
 * SLF4J를 사용하면 실제 로깅 구현체(Logback, Log4j 등)를
 * 코드 변경 없이 교체할 수 있습니다 (추상화의 장점!)
 */
public class LoggingListener implements PaymentListener {

    /**
     * Logger 인스턴스 생성
     *
     * [LoggerFactory.getLogger()]
     * - 이 클래스 전용 로거를 생성합니다
     * - 파라미터로 클래스를 전달하면 로그에 클래스명이 표시됩니다
     *
     * 출력 예시:
     * 2024-01-15 10:30:45 INFO  c.e.p.listener.LoggingListener - [LOG] payment completed: 9900.0
     *
     * [static final 사용 이유]
     * - static: 모든 인스턴스가 같은 Logger를 공유 (메모리 효율)
     * - final: Logger 인스턴스가 변경되지 않도록 보장
     */
    private static final Logger log = LoggerFactory.getLogger(LoggingListener.class);

    /**
     * 결제 완료 시 로그 기록
     *
     * [로그 레벨]
     * - log.trace(): 가장 상세한 디버깅 정보
     * - log.debug(): 개발 시 디버깅용
     * - log.info(): 일반적인 정보 (보통 이 레벨 사용)
     * - log.warn(): 경고 (문제가 될 수 있는 상황)
     * - log.error(): 에러 (예외 발생 시)
     *
     * [로그 메시지 포맷팅]
     * - {} 를 사용하면 뒤의 인자가 해당 위치에 삽입됩니다
     * - 문자열 연결(+)보다 효율적입니다 (지연 평가)
     *
     * 예시:
     * log.info("name: {}, age: {}", "홍길동", 20);
     * → "name: 홍길동, age: 20"
     *
     * @param result 결제 처리 결과
     */
    @Override
    public void onPaymentCompleted(PaymentResult result) {
        // 세금이 적용된 최종 금액을 로그에 기록
        log.info("[LOG] payment completed: {}", result.taxedAmount());
    }
}
