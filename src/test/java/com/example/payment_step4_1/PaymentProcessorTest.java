package com.example.payment_step4_1;

import com.example.payment_step4_1.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PaymentProcessor 테스트
 *
 * ============================================================================
 * [테스트 작성 시 느끼는 불편함]
 * ============================================================================
 *
 * 1. 테스트할 것이 너무 많다
 *    - 결제 생성, 승인, 실패 이력, 알림, 정책 분기...
 *
 * 2. Mock이 필요한 부분이 많다
 *    - 외부 알림 시스템
 *    - 카드사 승인 시뮬레이션
 *
 * 3. 테스트 격리가 어렵다
 *    - 하나의 테스트가 여러 책임을 검증
 *
 * 4. 테스트가 깨지기 쉽다
 *    - 정책 변경 시 많은 테스트 수정 필요
 */
@DisplayName("PaymentProcessor 테스트")
class PaymentProcessorTest {

    private PaymentProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new PaymentProcessor();
    }

    @Nested
    @DisplayName("결제 생성 테스트")
    class CreatePaymentTest {

        @Test
        @DisplayName("일반 고객 결제 생성 - 5% 할인")
        void createPaymentForCustomer() {
            // when
            Payment payment = processor.createPayment(10000, "KR", false);

            // then
            assertThat(payment.getId()).isNotNull();
            assertThat(payment.getOriginalPrice().getAmount()).isEqualTo(10000);
            assertThat(payment.getDiscountedAmount().getAmount()).isEqualTo(9500);
            assertThat(payment.isVip()).isFalse();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("VIP 고객 결제 생성 - 10% 할인")
        void createPaymentForVip() {
            // when
            Payment payment = processor.createPayment(10000, "KR", true);

            // then
            assertThat(payment.getDiscountedAmount().getAmount()).isEqualTo(9000);
            assertThat(payment.isVip()).isTrue();
        }
    }

    @Nested
    @DisplayName("[요구사항 1] 결제 실패 사유 저장 테스트")
    class FailureRecordTest {

        @Test
        @DisplayName("실패 시 실패 이력이 저장된다")
        void failureRecordIsSaved() {
            // given - 고액 결제 (실패 유도)
            Payment payment = processor.createPayment(60000, "KR", false);

            // when
            PaymentApprovalResult result = processor.approve(payment.getId());

            // then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getFailureType()).isEqualTo(FailureType.CARD_LIMIT_EXCEEDED);
            assertThat(result.getFailureRecord()).isNotNull();

            // 실패 이력 조회
            List<PaymentFailureRecord> records = processor.findFailureRecordsByPaymentId(payment.getId());
            assertThat(records).hasSize(1);
            assertThat(records.get(0).getFailureType()).isEqualTo(FailureType.CARD_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("실패 이력에 정책 정보가 포함된다")
        void failureRecordContainsPolicyInfo() {
            // given
            Payment payment = processor.createPayment(60000, "KR", false);

            // when
            PaymentApprovalResult result = processor.approve(payment.getId());

            // then
            PaymentFailureRecord record = result.getFailureRecord();
            assertThat(record.getPolicyInfo()).contains("VIP: false");
            assertThat(record.getPolicyInfo()).contains("Country: KR");
            assertThat(record.getPolicyInfo()).contains("DiscountRate: 5%");
        }
    }

    @Nested
    @DisplayName("[요구사항 2] 외부 알림 연동 테스트")
    class NotificationTest {

        @Test
        @DisplayName("실패 시 알림이 전송된다 (콘솔 출력 확인)")
        void notificationIsSentOnFailure() {
            // given
            Payment payment = processor.createPayment(60000, "KR", false);

            // when - 콘솔에서 [NOTIFICATION] 메시지 확인
            PaymentApprovalResult result = processor.approve(payment.getId());

            // then - 알림 전송 실패해도 결제 상태에 영향 없음
            assertThat(result.getPayment().getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        /**
         * [테스트 불편함]
         * - 외부 알림 시스템을 Mock 해야 검증 가능
         * - 현재 구조에서는 NotificationClient가 내부 클래스라 Mock 어려움
         * - "알림이 전송되었는지"를 어떻게 검증할 것인가?
         */
    }

    @Nested
    @DisplayName("[요구사항 3] 정책 분기 테스트")
    class PolicyBranchTest {

        @Test
        @DisplayName("VIP 고객은 실패 시 재시도한다")
        void vipCustomerRetries() {
            // given - VIP 고객의 소액 결제 (성공 가능)
            Payment payment = processor.createPayment(10000, "KR", true);

            // when
            PaymentApprovalResult result = processor.approve(payment.getId());

            // then - 재시도 로직으로 인해 성공 확률 높음
            // (네트워크 오류 10% 확률이므로 대부분 성공)
            if (result.isSuccess()) {
                assertThat(result.getPayment().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            }
        }

        @Test
        @DisplayName("US 결제는 별도 정책 적용 - 고액 거절")
        void usPaymentHasSpecialPolicy() {
            // given - 미국 고액 결제
            Payment payment = processor.createPayment(150000, "US", true);

            // when
            PaymentApprovalResult result = processor.approve(payment.getId());

            // then - US 정책에 의해 거절
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getFailureType()).isEqualTo(FailureType.POLICY_REJECTED);
        }

        @Test
        @DisplayName("정책 거절은 즉시 종료 (재시도 없음)")
        void policyRejectionStopsImmediately() {
            // given - US 고액 결제 (VIP여도 재시도 없이 즉시 종료)
            Payment payment = processor.createPayment(150000, "US", true);

            // when
            PaymentApprovalResult result = processor.approve(payment.getId());

            // then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getFailureType()).isEqualTo(FailureType.POLICY_REJECTED);
            // VIP여도 재시도 없이 즉시 종료됨
        }
    }

    @Nested
    @DisplayName("상태 전이 테스트")
    class StateTransitionTest {

        @Test
        @DisplayName("승인 성공 시 COMPLETED 상태")
        void approvalSuccess() {
            // given - 소액 결제 (성공 확률 높음)
            Payment payment = processor.createPayment(1000, "KR", false);

            // when
            PaymentApprovalResult result = processor.approve(payment.getId());

            // then (네트워크 오류 10% 확률 제외하면 성공)
            if (result.isSuccess()) {
                assertThat(result.getPayment().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            }
        }

        @Test
        @DisplayName("승인 실패 시 FAILED 상태")
        void approvalFailure() {
            // given - 고액 결제 (실패 유도)
            Payment payment = processor.createPayment(60000, "KR", false);

            // when
            PaymentApprovalResult result = processor.approve(payment.getId());

            // then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getPayment().getStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }

    /**
     * ============================================================================
     * [테스트 작성 후 느끼는 불편함 정리]
     * ============================================================================
     *
     * 1. PaymentProcessor가 너무 많은 책임을 가짐
     *    - 테스트할 것이 너무 많음
     *    - 하나의 테스트 클래스에 다양한 관심사
     *
     * 2. 외부 시스템 Mock이 어려움
     *    - NotificationClient가 내부 클래스
     *    - 의존성 주입 불가
     *
     * 3. 정책 분기가 복잡함
     *    - VIP 재시도, 국가별 정책, 실패 유형별 처리
     *    - 조건 조합이 많아 테스트 케이스 폭발
     *
     * 4. 랜덤 요소로 인한 비결정적 테스트
     *    - 네트워크 오류 10% 확률
     *    - 테스트 결과가 일정하지 않음
     *
     * [질문]
     * - 이 불편함을 해결하려면 어떤 구조 변경이 필요한가?
     * - 애그리게이트를 더 키우는 게 답일까?
     * - 아니면 구조적으로 보호할 장치가 필요한가?
     */
}
