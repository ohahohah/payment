package com.example.payment_step4_1.domain.model;

import java.time.LocalDateTime;

/**
 * PaymentFailureRecord - 결제 실패 이력
 *
 * [요구사항 1]
 * 결제 승인 실패 시 실패 사유를 저장해야 함
 * - 실패 유형 (FailureType)
 * - 실패 시점의 결제 금액
 * - 실패 시점의 정책 정보
 *
 * [질문 - 이 실패 이력은 Payment의 상태인가?]
 * - Payment 애그리게이트 안에 실패 이력을 들고 있어야 하는가?
 * - 실패 이력은 Payment의 불변식인가, 기록용 데이터인가?
 *
 * [현재 구현]
 * 일단 별도 클래스로 분리했지만, 어디에 저장할지 애매함
 */
public class PaymentFailureRecord {

    private Long id;
    private final Long paymentId;
    private final FailureType failureType;
    private final Money amountAtFailure;
    private final String policyInfo;
    private final LocalDateTime failedAt;

    public PaymentFailureRecord(Long paymentId, FailureType failureType,
                                 Money amountAtFailure, String policyInfo) {
        this.paymentId = paymentId;
        this.failureType = failureType;
        this.amountAtFailure = amountAtFailure;
        this.policyInfo = policyInfo;
        this.failedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Long getPaymentId() { return paymentId; }
    public FailureType getFailureType() { return failureType; }
    public Money getAmountAtFailure() { return amountAtFailure; }
    public String getPolicyInfo() { return policyInfo; }
    public LocalDateTime getFailedAt() { return failedAt; }

    public void assignId(Long id) {
        this.id = id;
    }
}
