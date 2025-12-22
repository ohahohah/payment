package com.example.payment_ul.policy.discount;

/**
 * CustomerDiscountPolicy - 고객 할인 정책 인터페이스
 *
 * 고객 유형에 따라 할인을 적용하는 정책을 정의합니다.
 */
public interface CustomerDiscountPolicy {

    /**
     * 고객 유형에 따른 할인을 적용합니다
     *
     * @param originalPrice 원래 가격
     * @param isVip VIP 고객 여부
     * @return 할인 적용 후 금액
     */
    double apply(double originalPrice, boolean isVip);
}
