package com.example.payment_ul.policy.tax;

/**
 * ====================================================================
 * TaxPolicy - 세금 정책 인터페이스 (전략 패턴)
 * ====================================================================
 *
 * [유비쿼터스 랭귀지 패키지 - 독립 실행용]
 * - payment_ul 패키지 전용 세금 정책입니다
 */
public interface TaxPolicy {

    /**
     * 세금을 적용하여 최종 금액을 계산합니다
     *
     * @param discountedPrice 할인 적용 후 가격
     * @return 세금 적용 후 최종 가격
     */
    double apply(double discountedPrice);
}
