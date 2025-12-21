package com.example.payment.listener;

import com.example.payment.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ====================================================================
 * SettlementListener - 정산 리스너 (옵저버 패턴의 ConcreteObserver)
 * ====================================================================
 *
 * [이 클래스의 역할]
 * - 결제가 완료되면 정산 요청이 필요한지 판단합니다
 * - 결제 금액이 일정 금액 이상이면 정산 요청을 발송합니다
 * - PaymentListener 인터페이스의 구현체입니다
 *
 * [@Component 어노테이션] (Spring Boot 권장 방식)
 * - 이 클래스를 스프링 빈으로 자동 등록합니다
 * - 스프링이 List<PaymentListener>를 주입할 때 자동으로 수집됩니다
 * - 새 리스너 추가 시 @Component만 붙이면 자동 등록 (OCP 원칙)
 *
 * [정산 (Settlement)이란?]
 * - 결제된 금액을 판매자에게 입금하는 과정입니다
 * - 대금 정산, 매출 정산이라고도 합니다
 * - 일반적으로 결제대행사(PG)가 수수료를 제외하고 입금합니다
 *
 * [비즈니스 규칙]
 * - 10만원 이상 결제 시 즉시 정산 요청 발송
 * - 10만원 미만은 일괄 정산 처리 (이 예제에서는 처리하지 않음)
 *
 * [옵저버 패턴의 활용 예]
 * 이처럼 결제 완료 이벤트에 여러 리스너를 등록하면:
 * - LoggingListener: 로그 기록
 * - SettlementListener: 정산 처리
 * - NotificationListener: 고객 알림 (추가 가능)
 * - PointListener: 포인트 적립 (추가 가능)
 *
 * 각 관심사를 분리하여 관리할 수 있습니다 (단일 책임 원칙, SRP)
 */
@Component
public class SettlementListener implements PaymentListener {

    /**
     * Logger 인스턴스
     */
    private static final Logger log = LoggerFactory.getLogger(SettlementListener.class);

    /**
     * 결제 완료 시 정산 요청 처리
     *
     * [조건부 처리]
     * - 10만원(100,000원) 이상일 때만 정산 요청을 발송합니다
     * - 실제 서비스에서는 외부 정산 시스템에 API 호출을 할 것입니다
     *
     * [숫자 리터럴 표기]
     * - 100000 대신 100_000으로 쓸 수도 있습니다 (Java 7+)
     * - 언더스코어로 자릿수를 구분하면 가독성이 좋아집니다
     * - 예: 1_000_000 (100만)
     *
     * @param result 결제 처리 결과
     */
    @Override
    public void onPaymentCompleted(PaymentResult result) {
        // 최종 결제 금액이 10만원 이상인 경우
        if (result.taxedAmount() > 100000) {
            // 정산 요청 발송 (실제로는 외부 API 호출 등)
            log.info("정산 요청 발송");

            // 실제 구현 예시 (주석):
            // settlementService.requestSettlement(
            //     result.taxedAmount(),
            //     merchantId,
            //     transactionId
            // );
        }
        // 10만원 미만은 일괄 정산을 위해 아무 처리도 하지 않음
    }
}
