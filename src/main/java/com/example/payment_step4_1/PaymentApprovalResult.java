package com.example.payment_step4_1;

import com.example.payment_step4_1.domain.model.FailureType;
import com.example.payment_step4_1.domain.model.Payment;
import com.example.payment_step4_1.domain.model.PaymentFailureRecord;

/**
 * PaymentApprovalResult - 결제 승인 결과
 *
 * [질문]
 * - 이 결과 객체는 도메인 객체인가, DTO인가?
 * - 어디에 위치해야 하는가?
 */
public class PaymentApprovalResult {

    private final boolean success;
    private final Payment payment;
    private final FailureType failureType;
    private final PaymentFailureRecord failureRecord;

    private PaymentApprovalResult(boolean success, Payment payment,
                                   FailureType failureType, PaymentFailureRecord failureRecord) {
        this.success = success;
        this.payment = payment;
        this.failureType = failureType;
        this.failureRecord = failureRecord;
    }

    public static PaymentApprovalResult success(Payment payment) {
        return new PaymentApprovalResult(true, payment, null, null);
    }

    public static PaymentApprovalResult failure(Payment payment, FailureType failureType,
                                                 PaymentFailureRecord failureRecord) {
        return new PaymentApprovalResult(false, payment, failureType, failureRecord);
    }

    public boolean isSuccess() { return success; }
    public Payment getPayment() { return payment; }
    public FailureType getFailureType() { return failureType; }
    public PaymentFailureRecord getFailureRecord() { return failureRecord; }
}
