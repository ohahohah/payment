package com.example.payment.policy.discount;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * ====================================================================
 * DefaultDiscountPolicy - 기본 할인 정책 구현체 (전략 패턴의 ConcreteStrategy)
 * ====================================================================
 *
 * [이 클래스의 역할]
 * - DiscountPolicy 인터페이스의 구체적인 구현체입니다
 * - 기본 할인 규칙을 정의합니다:
 *   - VIP 고객: 15% 할인 (0.85 적용)
 *   - 일반 고객: 10% 할인 (0.90 적용)
 *
 * [implements 키워드]
 * - 인터페이스를 구현할 때 사용합니다
 * - implements 뒤에 있는 인터페이스의 모든 메서드를 반드시 구현해야 합니다
 * - 클래스는 여러 인터페이스를 구현할 수 있습니다 (다중 상속과 유사)
 *   예: class MyClass implements Interface1, Interface2 { }
 *
 * [@Component 어노테이션] (Spring Boot 권장 방식)
 * - 이 클래스를 스프링 빈으로 자동 등록합니다
 * - 스프링 부트의 컴포넌트 스캔이 이 클래스를 찾아서 빈으로 등록합니다
 * - @Configuration + @Bean 방식보다 Spring Boot에서 권장됩니다
 *
 * [@Primary 어노테이션]
 * - 같은 타입(DiscountPolicy)의 빈이 여러 개 있을 때 기본으로 사용됩니다
 * - 다른 할인 정책이 추가되어도 이 정책이 기본으로 주입됩니다
 *
 * [전략 패턴에서의 역할]
 * - ConcreteStrategy (구체적 전략) 역할을 합니다
 * - 할인이라는 "전략"의 구체적인 "알고리즘"을 제공합니다
 */
@Component
@Primary
public class DefaultDiscountPolicy implements DiscountPolicy {

    /**
     * 할인 정책 적용
     *
     * [@Override 어노테이션]
     * - 부모 클래스나 인터페이스의 메서드를 오버라이드(재정의)함을 명시합니다
     * - 컴파일러가 실제로 오버라이드가 맞는지 검사합니다
     * - 오타로 인한 버그를 방지할 수 있습니다
     *   예: apply()를 appply()로 잘못 쓰면 컴파일 에러 발생
     *
     * [비즈니스 로직]
     * - VIP 고객: 원가의 85%를 지불 (15% 할인)
     * - 일반 고객: 원가의 90%를 지불 (10% 할인)
     *
     * [부동소수점 오차 처리]
     * - double 연산은 IEEE 754 표준을 따르며 정밀도 한계가 있습니다
     * - Math.round()로 반올림하여 금액 계산의 정확성을 보장합니다
     * - 실무에서는 BigDecimal 사용을 권장합니다
     *
     * @param originalPrice 원래 가격
     * @param isVip VIP 여부
     * @return 할인 적용된 가격 (반올림 처리)
     */
    @Override
    public double apply(double originalPrice, boolean isVip) {
        if (isVip) {
            // VIP 고객은 15% 할인 (원가의 85% 지불)
            return Math.round(originalPrice * 0.85);
        }
        // 일반 고객은 10% 할인 (원가의 90% 지불)
        return Math.round(originalPrice * 0.90);
    }
}
