package com.example.payment.policy.tax;

/**
 * ====================================================================
 * UsTaxPolicy - 미국 세금 정책 구현체
 * ====================================================================
 *
 * [이 클래스의 역할]
 * - 미국의 판매세(Sales Tax) 7%를 적용합니다
 * - TaxPolicy 인터페이스의 구체적인 구현체입니다
 *
 * [미국 판매세 (Sales Tax)]
 * - 미국은 연방 차원의 부가가치세가 없습니다
 * - 주(State)마다 판매세율이 다릅니다 (0% ~ 약 10%)
 * - 이 예제에서는 평균적인 7%를 사용합니다
 *
 * [다형성 (Polymorphism)]
 * - KoreaTaxPolicy와 UsTaxPolicy는 같은 TaxPolicy 인터페이스를 구현합니다
 * - 사용하는 측(PaymentProcessor)에서는 TaxPolicy 타입으로 받습니다
 * - 어떤 구현체가 들어오든 apply() 메서드를 호출할 수 있습니다
 *
 * 예시:
 * TaxPolicy policy = new KoreaTaxPolicy();  // 가능
 * TaxPolicy policy = new UsTaxPolicy();     // 가능
 * double result = policy.apply(1000);       // 둘 다 가능
 *
 * 이것이 다형성의 핵심입니다:
 * "같은 인터페이스, 다른 구현, 동일한 사용 방법"
 */
public class UsTaxPolicy implements TaxPolicy {

    /**
     * 미국 판매세율 (7%)
     */
    private static final double TAX_RATE = 0.07;

    /**
     * 세금 적용
     *
     * [KoreaTaxPolicy와의 비교]
     * - 같은 메서드 시그니처 (apply)
     * - 같은 계산 공식 (가격 × (1 + 세율))
     * - 다른 세율 (한국 10%, 미국 7%)
     *
     * 이처럼 전략 패턴을 사용하면:
     * - 알고리즘의 "뼈대"는 같고
     * - 세부 구현만 다른 경우를 깔끔하게 처리할 수 있습니다
     *
     * @param discountedPrice 할인 적용 후 가격
     * @return 세금 포함 최종 가격
     */
    @Override
    public double apply(double discountedPrice) {
        return discountedPrice * (1 + TAX_RATE);
    }
}
