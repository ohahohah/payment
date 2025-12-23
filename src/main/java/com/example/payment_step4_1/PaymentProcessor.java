package com.example.payment_step4_1;

import com.example.payment_step4_1.domain.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * PaymentProcessor - 결제 처리기
 *
 * ============================================================================
 * [실습 가이드]
 * ============================================================================
 *
 * 이 클래스는 의도적으로 "모든 책임"을 담고 있습니다.
 * 구현하면서 다음 불편함을 느껴보세요:
 *
 * 1. "여기 넣기엔 이상하다"
 * 2. "애그리게이트 책임은 아닌 것 같다"
 * 3. "Processor가 점점 커진다"
 * 4. "이걸 어디에 둬야 하지?"
 *
 * ============================================================================
 * [담당 책임 (의도적으로 과도함)]
 * ============================================================================
 *
 * - 결제 생성 및 저장
 * - 결제 승인 시도
 * - 실패 사유 저장 (요구사항 1)
 * - 외부 알림 전송 (요구사항 2)
 * - 정책별 분기 처리 (요구사항 3)
 * - VIP 재시도 로직
 * - 국가별 정책 적용
 */
@Service
public class PaymentProcessor {

    // In-Memory 저장소 (실습용)
    private final Map<Long, Payment> paymentStore = new HashMap<>();
    private final List<PaymentFailureRecord> failureRecords = new ArrayList<>();
    private long paymentIdSequence = 1L;
    private long failureRecordIdSequence = 1L;

    // 외부 알림 시스템 (실습용 - 콘솔 출력)
    // [질문] 이 외부 시스템 호출을 여기서 해도 되는가?
    private final NotificationClient notificationClient = new NotificationClient();

    // ==========================================================================
    // 결제 생성
    // ==========================================================================

    public Payment createPayment(double amount, String countryCode, boolean isVip) {
        Money originalPrice = Money.of(amount);
        Country country = Country.of(countryCode);

        // 할인 계산
        double discountRate = isVip ? 0.10 : 0.05;
        Money discount = originalPrice.multiply(discountRate);
        Money discountedAmount = originalPrice.subtract(discount);

        // 세금 계산
        Money taxedAmount = discountedAmount.multiply(1.10);

        Payment payment = Payment.create(originalPrice, discountedAmount, taxedAmount, country, isVip);

        // 저장
        payment.assignId(paymentIdSequence++);
        paymentStore.put(payment.getId(), payment);

        return payment;
    }

    // ==========================================================================
    // 결제 승인 (핵심 비즈니스 로직)
    // ==========================================================================

    /**
     * 결제 승인 시도
     *
     * [요구사항 1] 실패 시 실패 사유 저장
     * [요구사항 2] 실패 시 외부 알림 전송
     * [요구사항 3] 정책별 분기 처리
     */
    public PaymentApprovalResult approve(Long paymentId) {
        Payment payment = findPaymentById(paymentId);

        // [요구사항 3] 국가별 정책 분기
        // [질문] 이 분기 로직은 도메인 규칙인가, 흐름 제어인가?
        if (payment.getCountry().isUS()) {
            return approveWithUSPolicy(payment);
        }

        // 기본 승인 로직
        return attemptApproval(payment, 1);
    }

    /**
     * 미국 정책으로 승인 시도
     * [요구사항 3] 특정 국가 결제는 별도 정책 적용
     */
    private PaymentApprovalResult approveWithUSPolicy(Payment payment) {
        System.out.println("[US Policy] 미국 결제는 추가 검증 수행");

        // 미국 정책: 추가 검증 후 승인 시도
        // [질문] 이 검증 로직은 어디에 있어야 하는가?
        if (payment.getTaxedAmount().getAmount() > 100000) {
            return handleFailure(payment, FailureType.POLICY_REJECTED, "US 고액 결제 정책 거절");
        }

        return attemptApproval(payment, 1);
    }

    /**
     * 승인 시도 (재시도 포함)
     *
     * [요구사항 3] VIP 고객은 실패 시 재시도 로직을 가짐
     */
    private PaymentApprovalResult attemptApproval(Payment payment, int attempt) {
        int maxAttempts = payment.isVip() ? 3 : 1;  // VIP는 3번까지 재시도

        System.out.println("[Approval] 승인 시도 " + attempt + "/" + maxAttempts);

        // 외부 카드사 승인 시뮬레이션
        FailureType failureType = simulateCardApproval(payment);

        if (failureType == null) {
            // 승인 성공
            payment.complete();
            return PaymentApprovalResult.success(payment);
        }

        // [요구사항 3] 특정 실패 유형은 즉시 종료
        if (failureType == FailureType.POLICY_REJECTED) {
            System.out.println("[Policy] 정책 거절 - 재시도 없이 즉시 종료");
            return handleFailure(payment, failureType, "정책 거절로 인한 즉시 종료");
        }

        // [요구사항 3] VIP 고객 재시도
        if (payment.isVip() && attempt < maxAttempts) {
            System.out.println("[VIP Retry] VIP 고객 재시도 중...");
            return attemptApproval(payment, attempt + 1);
        }

        // 최종 실패
        return handleFailure(payment, failureType, "최대 시도 횟수 초과");
    }

    /**
     * 카드사 승인 시뮬레이션
     *
     * [질문] 이 외부 시스템 호출 로직은 어디에 있어야 하는가?
     */
    private FailureType simulateCardApproval(Payment payment) {
        // 시뮬레이션: 금액에 따라 실패 유형 결정
        double amount = payment.getTaxedAmount().getAmount();

        if (amount > 50000 && !payment.isVip()) {
            return FailureType.CARD_LIMIT_EXCEEDED;
        }

        // 랜덤 네트워크 오류 (10% 확률)
        if (Math.random() < 0.1) {
            return FailureType.NETWORK_ERROR;
        }

        return null; // 성공
    }

    /**
     * 실패 처리
     *
     * [요구사항 1] 실패 사유 저장
     * [요구사항 2] 외부 알림 전송
     *
     * [질문]
     * - 이 실패 처리 로직이 Processor에 있는 게 맞는가?
     * - Payment 안에서 해도 되는가?
     */
    private PaymentApprovalResult handleFailure(Payment payment, FailureType failureType, String additionalInfo) {
        // 결제 상태 변경
        payment.fail();

        // [요구사항 1] 실패 이력 저장
        // [질문] 이 실패 이력은 Payment의 상태인가, 기록용 데이터인가?
        PaymentFailureRecord record = new PaymentFailureRecord(
                payment.getId(),
                failureType,
                payment.getTaxedAmount(),
                buildPolicyInfo(payment, additionalInfo)
        );
        record.assignId(failureRecordIdSequence++);
        failureRecords.add(record);

        // [요구사항 2] 외부 알림 전송
        // [질문] 외부 알림 호출을 여기서 해도 되는가?
        // [질문] 도메인이 외부 시스템을 알게 되는 순간 무엇이 깨지는가?
        sendFailureNotification(payment, failureType);

        return PaymentApprovalResult.failure(payment, failureType, record);
    }

    /**
     * 정책 정보 생성
     *
     * [요구사항 1] 실패 시점의 정책 정보도 함께 저장해야 함
     */
    private String buildPolicyInfo(Payment payment, String additionalInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("VIP: ").append(payment.isVip());
        sb.append(", Country: ").append(payment.getCountry().getCode());
        sb.append(", DiscountRate: ").append(payment.isVip() ? "10%" : "5%");
        if (additionalInfo != null) {
            sb.append(", Note: ").append(additionalInfo);
        }
        return sb.toString();
    }

    /**
     * 실패 알림 전송
     *
     * [요구사항 2]
     * - 알림 전송 실패가 결제 실패 자체에 영향을 주면 안 됨
     * - 알림 시스템은 추후 교체 가능함
     * - 알림 내용 포맷은 정책에 따라 달라질 수 있음
     *
     * [질문] Processor에서 호출하면 괜찮은가?
     */
    private void sendFailureNotification(Payment payment, FailureType failureType) {
        try {
            // 알림 내용 포맷 (정책에 따라 달라짐)
            String message = formatNotificationMessage(payment, failureType);

            // 외부 알림 시스템 호출
            notificationClient.send(message);

        } catch (Exception e) {
            // [요구사항 2] 알림 전송 실패로 결제 상태가 바뀌면 안 됨
            System.err.println("[Notification] 알림 전송 실패 (결제에 영향 없음): " + e.getMessage());
        }
    }

    /**
     * 알림 메시지 포맷
     *
     * [요구사항 2] 알림 내용 포맷은 정책에 따라 달라질 수 있음
     *
     * [질문] 이 포맷 로직은 어디에 있어야 하는가?
     */
    private String formatNotificationMessage(Payment payment, FailureType failureType) {
        // VIP 고객은 다른 포맷
        if (payment.isVip()) {
            return String.format("[VIP 고객 결제 실패 알림] " +
                            "결제 ID: %d, 실패 유형: %s, 금액: %s, 국가: %s",
                    payment.getId(),
                    failureType.getDescription(),
                    payment.getTaxedAmount(),
                    payment.getCountry());
        }

        return String.format("[결제 실패 알림] 결제 ID: %d, 실패: %s",
                payment.getId(), failureType.getDescription());
    }

    // ==========================================================================
    // 조회
    // ==========================================================================

    public Payment findPaymentById(Long id) {
        Payment payment = paymentStore.get(id);
        if (payment == null) {
            throw new IllegalArgumentException("결제를 찾을 수 없습니다: " + id);
        }
        return payment;
    }

    public List<Payment> findAllPayments() {
        return new ArrayList<>(paymentStore.values());
    }

    public List<PaymentFailureRecord> findFailureRecordsByPaymentId(Long paymentId) {
        return failureRecords.stream()
                .filter(r -> r.getPaymentId().equals(paymentId))
                .toList();
    }

    public List<PaymentFailureRecord> findAllFailureRecords() {
        return new ArrayList<>(failureRecords);
    }

    // ==========================================================================
    // 내부 클래스: 알림 클라이언트 (실습용)
    // ==========================================================================

    /**
     * NotificationClient - 외부 알림 시스템 클라이언트
     *
     * [요구사항 2] 알림 시스템은 추후 교체 가능함
     *
     * [질문] 이 클래스가 Processor 안에 있는 게 맞는가?
     */
    private static class NotificationClient {
        public void send(String message) {
            // 실제로는 외부 API 호출
            System.out.println("[NOTIFICATION] " + message);
        }
    }
}
