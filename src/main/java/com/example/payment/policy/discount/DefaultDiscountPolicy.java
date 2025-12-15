package com.example.payment.policy.discount;

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
 * [왜 @Component나 @Service가 없나요?]
 * - 이 클래스는 PaymentConfig에서 @Bean으로 등록됩니다
 * - @Component를 사용하면 컴포넌트 스캔으로 빈 등록
 * - @Bean을 사용하면 Configuration 클래스에서 명시적으로 빈 등록
 * - 두 방법 모두 가능하며, 상황에 따라 선택합니다
 *
 * [전략 패턴에서의 역할]
 * - ConcreteStrategy (구체적 전략) 역할을 합니다
 * - 할인이라는 "전략"의 구체적인 "알고리즘"을 제공합니다
 */
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
     * @param originalPrice 원래 가격
     * @param isVip VIP 여부
     * @return 할인 적용된 가격
     */
    @Override
    public double apply(double originalPrice, boolean isVip) {
        if (isVip) {
            // VIP 고객은 15% 할인 (원가의 85% 지불)
            return originalPrice * 0.85;
        }
        // 일반 고객은 10% 할인 (원가의 90% 지불)
        return originalPrice * 0.90;
    }
}
