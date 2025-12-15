package com.example.payment.policy.discount;

/**
 * ====================================================================
 * DiscountPolicy - 할인 정책 인터페이스 (전략 패턴의 Strategy)
 * ====================================================================
 *
 * [전략 패턴 (Strategy Pattern)이란?]
 * - GoF 디자인 패턴 중 하나로, 행위(알고리즘)를 캡슐화하여 교체 가능하게 만드는 패턴입니다
 * - 같은 문제를 해결하는 여러 알고리즘이 있을 때, 런타임에 적절한 것을 선택할 수 있습니다
 *
 * [전략 패턴의 구성 요소]
 * 1. Strategy (전략) - 이 인터페이스
 *    - 알고리즘의 공통 인터페이스를 정의합니다
 *
 * 2. ConcreteStrategy (구체적 전략) - DefaultDiscountPolicy 등
 *    - Strategy 인터페이스를 구현한 실제 알고리즘입니다
 *
 * 3. Context (문맥) - PaymentProcessor
 *    - Strategy를 사용하는 클래스입니다
 *    - 어떤 전략을 사용할지는 외부에서 주입받습니다
 *
 * [전략 패턴의 장점]
 * 1. 개방-폐쇄 원칙 (OCP): 새 할인 정책 추가 시 기존 코드 수정 불필요
 * 2. 단일 책임 원칙 (SRP): 각 전략 클래스는 하나의 할인 로직만 담당
 * 3. 테스트 용이성: 각 전략을 독립적으로 테스트 가능
 * 4. 런타임 교체: 실행 중에도 전략을 바꿀 수 있음
 *
 * [스프링에서 전략 패턴 활용]
 * - 인터페이스를 빈으로 등록하고, 구현체 중 하나를 주입받습니다
 * - @Primary, @Qualifier 등으로 어떤 구현체를 주입할지 결정합니다
 *
 * 예시:
 * @Autowired
 * private DiscountPolicy discountPolicy;  // @Primary로 지정된 빈이 주입됨
 *
 * @Autowired
 * @Qualifier("vipDiscountPolicy")
 * private DiscountPolicy vipPolicy;  // 이름으로 특정 빈을 지정
 */
public interface DiscountPolicy {

    /**
     * 할인 정책을 적용하여 할인된 금액을 계산합니다
     *
     * [인터페이스 메서드 특징]
     * - 구현부(body)가 없습니다 (세미콜론으로 끝남)
     * - 이 인터페이스를 구현하는 클래스가 실제 로직을 제공해야 합니다
     * - 기본적으로 public abstract 입니다 (생략 가능)
     *
     * @param originalPrice 원래 가격
     * @param isVip VIP 고객 여부 (VIP는 더 많은 할인을 받을 수 있음)
     * @return 할인 적용 후 금액
     */
    double apply(double originalPrice, boolean isVip);
}
