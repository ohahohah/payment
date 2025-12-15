package com.example.payment.factory;

import com.example.payment.policy.discount.DefaultDiscountPolicy;
import com.example.payment.policy.discount.DiscountPolicy;
import com.example.payment.policy.tax.KoreaTaxPolicy;
import com.example.payment.policy.tax.TaxPolicy;
import com.example.payment.policy.tax.UsTaxPolicy;
import org.springframework.stereotype.Component;

/**
 * ====================================================================
 * DefaultPaymentProcessorFactory - 기본 결제 처리기 팩토리 (팩토리 메서드 패턴의 ConcreteCreator)
 * ====================================================================
 *
 * [@Component 어노테이션]
 * - 이 클래스를 스프링 빈으로 등록합니다
 * - 컴포넌트 스캔 시 자동으로 발견되어 빈으로 등록됩니다
 *
 * [@Component vs @Service vs @Repository]
 * - @Component: 일반적인 컴포넌트
 * - @Service: 비즈니스 로직 서비스
 * - @Repository: 데이터 액세스 계층
 * - 이 클래스는 "팩토리" 역할이므로 일반 @Component를 사용합니다
 *
 * [extends 키워드]
 * - PaymentProcessorFactory를 상속받습니다
 * - 부모 클래스의 메서드와 필드를 물려받습니다
 * - 추상 메서드를 반드시 구현해야 합니다
 *
 * [팩토리 메서드 패턴에서의 역할]
 * - ConcreteCreator (구체적 생성자) 역할을 합니다
 * - 실제로 어떤 정책 객체를 생성할지 결정합니다
 */
@Component
public class DefaultPaymentProcessorFactory extends PaymentProcessorFactory {

    /**
     * 할인 정책 생성
     *
     * [@Override]
     * - 부모 클래스의 추상 메서드를 구현함을 나타냅니다
     * - 부모의 createDiscountPolicy()를 오버라이드합니다
     *
     * [구현 내용]
     * - 기본 할인 정책(DefaultDiscountPolicy)을 생성합니다
     * - 필요하다면 다른 팩토리 클래스에서 다른 정책을 반환할 수 있습니다
     *
     * @return DefaultDiscountPolicy 인스턴스
     */
    @Override
    protected DiscountPolicy createDiscountPolicy() {
        return new DefaultDiscountPolicy();
    }

    /**
     * 세금 정책 생성 (국가별)
     *
     * [switch 표현식 (Java 14+)]
     * - 기존 switch 문의 개선된 버전입니다
     * - -> 를 사용하여 간결하게 작성합니다
     * - break 문이 필요 없습니다 (fall-through 없음)
     * - 값을 반환할 수 있습니다
     *
     * [기존 switch 문 방식]
     * switch (country) {
     *     case "KR":
     *         return new KoreaTaxPolicy();
     *     case "US":
     *         return new UsTaxPolicy();
     *     default:
     *         return new KoreaTaxPolicy();
     * }
     *
     * [switch 표현식 방식 (이 코드)]
     * return switch (country) {
     *     case "KR" -> new KoreaTaxPolicy();
     *     case "US" -> new UsTaxPolicy();
     *     default -> new KoreaTaxPolicy();
     * };
     *
     * @param country 국가 코드
     * @return 해당 국가의 TaxPolicy 인스턴스
     */
    @Override
    protected TaxPolicy createTaxPolicy(String country) {
        // Java 14+ switch 표현식 사용
        return switch (country) {
            case "KR" -> new KoreaTaxPolicy();   // 한국: 10% 부가세
            case "US" -> new UsTaxPolicy();      // 미국: 7% 판매세
            default -> new KoreaTaxPolicy();     // 기본값: 한국 정책
        };
    }
}
