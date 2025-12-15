package com.example.payment.factory;

import com.example.payment.listener.PaymentListener;
import com.example.payment.policy.discount.DiscountPolicy;
import com.example.payment.policy.tax.TaxPolicy;
import com.example.payment.service.PaymentProcessor;

import java.util.List;

/**
 * ====================================================================
 * PaymentProcessorFactory - 결제 처리기 팩토리 (팩토리 메서드 패턴의 Creator)
 * ====================================================================
 *
 * [팩토리 메서드 패턴 (Factory Method Pattern)이란?]
 * - GoF 디자인 패턴 중 하나로, 객체 생성을 위한 인터페이스를 정의하되
 *   어떤 클래스의 인스턴스를 생성할지는 서브클래스가 결정하게 하는 패턴입니다
 * - "객체 생성의 책임을 서브클래스에 위임"합니다
 *
 * [팩토리 메서드 패턴의 구성 요소]
 * 1. Creator (생성자) - PaymentProcessorFactory (이 클래스)
 *    - 팩토리 메서드를 선언하는 추상 클래스입니다
 *    - 객체 생성의 "뼈대"를 정의합니다
 *
 * 2. ConcreteCreator (구체적 생성자) - DefaultPaymentProcessorFactory
 *    - Creator를 상속받아 팩토리 메서드를 구현합니다
 *    - 실제로 어떤 객체를 생성할지 결정합니다
 *
 * 3. Product (제품) - PaymentProcessor
 *    - 팩토리가 생성하는 객체입니다
 *
 * [팩토리 메서드 패턴의 장점]
 * 1. 객체 생성 로직 캡슐화: 생성 과정의 복잡함을 숨김
 * 2. 결합도 감소: 클라이언트는 구체적인 생성 과정을 몰라도 됨
 * 3. 확장 용이: 새로운 생성 방식은 새 팩토리 클래스로 추가
 *
 * [abstract 클래스란?]
 * - 직접 인스턴스를 생성할 수 없는 클래스입니다
 * - new PaymentProcessorFactory()는 불가능합니다
 * - 반드시 상속받아서 사용해야 합니다
 * - 일부 메서드만 구현하고 나머지는 서브클래스에 위임할 수 있습니다
 */
public abstract class PaymentProcessorFactory {

    /**
     * 템플릿 메서드 (Template Method)
     *
     * [템플릿 메서드 패턴]
     * - 알고리즘의 뼈대를 정의하고, 일부 단계를 서브클래스에 위임합니다
     * - 이 메서드가 "템플릿"이고, createDiscountPolicy(), createTaxPolicy()가 "위임"입니다
     *
     * [처리 순서]
     * 1. createDiscountPolicy() 호출 → 서브클래스가 구현
     * 2. createTaxPolicy() 호출 → 서브클래스가 구현
     * 3. PaymentProcessor 생성 후 반환
     *
     * 이렇게 하면 생성 과정의 "순서"는 이 클래스가 결정하고,
     * "어떤 정책을 사용할지"는 서브클래스가 결정합니다.
     *
     * @param country 국가 코드 (세금 정책 결정에 사용)
     * @param listeners 결제 리스너 목록
     * @return 생성된 PaymentProcessor 인스턴스
     */
    public PaymentProcessor create(String country, List<PaymentListener> listeners) {
        // 서브클래스가 구현할 팩토리 메서드 호출
        DiscountPolicy discountPolicy = createDiscountPolicy();
        TaxPolicy taxPolicy = createTaxPolicy(country);

        // PaymentProcessor 생성 및 반환
        return new PaymentProcessor(discountPolicy, taxPolicy, listeners);
    }

    /**
     * 할인 정책 생성 팩토리 메서드 (추상 메서드)
     *
     * [protected 접근 제어자]
     * - 같은 패키지 + 서브클래스에서만 접근 가능
     * - 외부에서는 직접 호출할 수 없음 (캡슐화)
     *
     * [abstract 메서드]
     * - 구현부가 없는 메서드입니다
     * - 서브클래스가 반드시 구현해야 합니다
     * - 구현하지 않으면 컴파일 에러 발생
     *
     * @return 생성된 DiscountPolicy 인스턴스
     */
    protected abstract DiscountPolicy createDiscountPolicy();

    /**
     * 세금 정책 생성 팩토리 메서드 (추상 메서드)
     *
     * [국가별 세금 정책 선택]
     * - country 파라미터에 따라 적절한 TaxPolicy를 생성합니다
     * - 어떤 정책을 생성할지는 서브클래스가 결정합니다
     *
     * @param country 국가 코드 (예: "KR", "US")
     * @return 생성된 TaxPolicy 인스턴스
     */
    protected abstract TaxPolicy createTaxPolicy(String country);
}
