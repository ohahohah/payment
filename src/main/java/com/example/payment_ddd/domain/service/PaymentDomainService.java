package com.example.payment_ddd.domain.service;

import com.example.payment_ddd.domain.model.Country;
import com.example.payment_ddd.domain.model.Money;
import com.example.payment_ddd.domain.model.Payment;
import com.example.payment_ddd.domain.policy.DiscountPolicy;
import com.example.payment_ddd.domain.policy.TaxPolicy;

import java.util.List;

/**
 * PaymentDomainService - 결제 도메인 서비스
 *
 * [Domain Service vs Application Service]
 *
 * Domain Service:
 * - 도메인 로직 중 특정 엔티티에 속하지 않는 것
 * - 여러 엔티티/값 객체를 조합하는 비즈니스 로직
 * - 상태를 갖지 않음 (Stateless)
 * - 예: 할인 + 세금 계산 (여러 정책 조합)
 *
 * Application Service:
 * - 유스케이스 조율 (트랜잭션, 이벤트 발행 등)
 * - 도메인 로직을 포함하지 않음
 * - 인프라 서비스 호출 (저장소, 메시징 등)
 *
 * [이 서비스가 Domain Service인 이유]
 * - 할인과 세금 계산은 순수한 비즈니스 로직
 * - Payment 엔티티 하나에 속하지 않고 여러 정책을 조합
 * - 인프라(DB, 메시지큐 등)에 의존하지 않음
 */
public class PaymentDomainService {

    private final DiscountPolicy discountPolicy;
    private final List<TaxPolicy> taxPolicies;

    public PaymentDomainService(DiscountPolicy discountPolicy, List<TaxPolicy> taxPolicies) {
        this.discountPolicy = discountPolicy;
        this.taxPolicies = taxPolicies;
    }

    /**
     * 결제 생성
     *
     * [도메인 로직 조합]
     * 1. 할인 정책 적용
     * 2. 세금 정책 적용
     * 3. Payment Aggregate 생성
     *
     * @param originalPrice 원래 가격
     * @param country 국가
     * @param isVip VIP 여부
     * @return 생성된 Payment
     */
    public Payment createPayment(Money originalPrice, Country country, boolean isVip) {
        // 1. 할인 적용
        Money discountedAmount = discountPolicy.applyDiscount(originalPrice, isVip);

        // 2. 국가별 세금 정책 찾기 및 적용
        TaxPolicy applicableTaxPolicy = findApplicableTaxPolicy(country);
        Money taxedAmount = applicableTaxPolicy.applyTax(discountedAmount);

        // 3. Payment Aggregate 생성
        return Payment.create(originalPrice, discountedAmount, taxedAmount, country, isVip);
    }

    /**
     * 적용 가능한 세금 정책 찾기
     */
    private TaxPolicy findApplicableTaxPolicy(Country country) {
        return taxPolicies.stream()
                .filter(policy -> policy.supports(country))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 국가에 적용 가능한 세금 정책이 없습니다: " + country));
    }
}
