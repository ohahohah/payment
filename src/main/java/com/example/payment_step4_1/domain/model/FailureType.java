package com.example.payment_step4_1.domain.model;

/**
 * FailureType - 결제 실패 유형
 *
 * [요구사항 1]
 * 실패 사유는 문자열이 아닌 구분된 유형으로 관리
 */
public enum FailureType {
    CARD_LIMIT_EXCEEDED("카드 한도 초과"),
    NETWORK_ERROR("네트워크 오류"),
    POLICY_REJECTED("정책상 거절");

    private final String description;

    FailureType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
