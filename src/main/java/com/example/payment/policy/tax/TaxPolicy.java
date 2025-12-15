package com.example.payment.policy.tax;

/**
 * ====================================================================
 * TaxPolicy - 세금 정책 인터페이스 (전략 패턴의 Strategy)
 * ====================================================================
 *
 * [이 인터페이스의 역할]
 * - 국가별로 다른 세금 정책을 추상화합니다
 * - 할인된 가격에 세금을 적용하는 규칙을 정의합니다
 *
 * [전략 패턴 적용]
 * 세금 계산도 할인과 마찬가지로 전략 패턴을 사용합니다:
 *
 * - Strategy (전략): TaxPolicy (이 인터페이스)
 * - ConcreteStrategy (구체적 전략): KoreaTaxPolicy, UsTaxPolicy
 * - Context (문맥): PaymentProcessor
 *
 * [국가별 세금 정책 예시]
 * - 한국 (KR): 10% 부가가치세 (VAT)
 * - 미국 (US): 7% 판매세 (Sales Tax)
 * - 유럽연합 (EU): 15~27% 부가가치세 (국가마다 다름)
 *
 * [확장성]
 * 새로운 국가의 세금 정책을 추가하려면:
 * 1. TaxPolicy 인터페이스를 구현하는 새 클래스 생성 (예: EuTaxPolicy)
 * 2. PaymentConfig에서 빈으로 등록
 * 3. 필요한 곳에서 주입받아 사용
 *
 * 기존 코드(TaxPolicy, PaymentProcessor 등)는 수정하지 않아도 됩니다!
 * 이것이 개방-폐쇄 원칙(OCP)입니다.
 */
public interface TaxPolicy {

    /**
     * 세금을 적용하여 최종 금액을 계산합니다
     *
     * [주의사항]
     * - 할인이 적용된 가격(discountedPrice)을 받습니다 (원가가 아님!)
     * - 세금은 할인 후 가격에 적용됩니다
     *
     * 계산 순서:
     * 원가 → 할인 적용 → 세금 적용 → 최종 가격
     *
     * @param discountedPrice 할인 적용 후 가격
     * @return 세금 적용 후 최종 가격
     */
    double apply(double discountedPrice);
}
