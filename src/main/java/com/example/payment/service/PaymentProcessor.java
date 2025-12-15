package com.example.payment.service;

import com.example.payment.dto.PaymentResult;
import com.example.payment.listener.PaymentListener;
import com.example.payment.policy.discount.DiscountPolicy;
import com.example.payment.policy.tax.TaxPolicy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ====================================================================
 * PaymentProcessor - 결제 처리 서비스 (핵심 비즈니스 로직)
 * ====================================================================
 *
 * [@Service 어노테이션]
 * - 이 클래스가 서비스 계층의 컴포넌트임을 나타냅니다
 * - @Component의 특수화된 형태입니다
 * - 스프링이 컴포넌트 스캔으로 이 클래스를 빈으로 등록합니다
 *
 * [@Service vs @Component]
 * - 기능적으로는 동일합니다 (둘 다 빈으로 등록)
 * - @Service는 비즈니스 로직을 담는 클래스에 사용
 * - @Repository는 데이터 액세스 계층에 사용
 * - @Controller는 웹 요청 처리 계층에 사용
 * - 이렇게 구분하면 코드의 의도가 명확해집니다
 *
 * [이 클래스의 역할]
 * - 결제 처리의 핵심 비즈니스 로직을 담당합니다
 * - 할인 적용 → 세금 적용 → 결과 반환 순서로 처리합니다
 * - 전략 패턴과 옵저버 패턴을 사용합니다
 *
 * [디자인 패턴 적용]
 * 1. 전략 패턴 (Strategy Pattern)
 *    - DiscountPolicy, TaxPolicy를 주입받아 사용
 *    - Context 역할을 합니다
 *
 * 2. 옵저버 패턴 (Observer Pattern)
 *    - PaymentListener 목록을 관리
 *    - Subject 역할을 합니다
 */
@Service
public class PaymentProcessor {

    /**
     * 의존성 주입을 위한 필드들
     *
     * [final 키워드]
     * - 생성자에서 한번 초기화하면 변경 불가
     * - 불변성(Immutability)을 보장합니다
     * - 스프링에서 권장하는 방식입니다
     */
    private final DiscountPolicy discountPolicy;
    private final TaxPolicy taxPolicy;
    private final List<PaymentListener> listeners;

    /**
     * 생성자 주입 (Constructor Injection)
     *
     * [의존성 주입 (Dependency Injection, DI)]
     * - 객체가 필요로 하는 의존성을 외부에서 넣어주는 것입니다
     * - 직접 new로 생성하지 않고, 스프링이 대신 주입해줍니다
     *
     * [생성자 주입의 장점]
     * 1. 불변성 보장: final 필드 사용 가능
     * 2. 필수 의존성 명시: 생성자 파라미터로 필수 의존성이 드러남
     * 3. 테스트 용이성: 테스트 시 Mock 객체 주입 용이
     * 4. 순환 참조 방지: 컴파일 타임에 순환 참조 감지 가능
     *
     * [생성자가 하나일 때]
     * - @Autowired 어노테이션 생략 가능 (Spring 4.3+)
     * - 스프링이 자동으로 의존성을 주입합니다
     *
     * [@Autowired vs 생성자 주입]
     * - @Autowired는 필드/세터 주입에 사용 가능
     * - 생성자 주입이 더 권장됨 (위의 장점들 때문에)
     *
     * @param discountPolicy 할인 정책 (전략 패턴의 Strategy)
     * @param taxPolicy 세금 정책 (전략 패턴의 Strategy)
     * @param listeners 결제 리스너 목록 (옵저버 패턴의 Observer들)
     */
    public PaymentProcessor(DiscountPolicy discountPolicy,
                            TaxPolicy taxPolicy,
                            List<PaymentListener> listeners) {
        this.discountPolicy = discountPolicy;
        this.taxPolicy = taxPolicy;
        this.listeners = listeners;
    }

    /**
     * 결제 처리 메인 로직
     *
     * [처리 순서]
     * 1. 입력값 검증 (유효하지 않으면 예외 발생)
     * 2. 할인 정책 적용 (전략 패턴)
     * 3. 세금 정책 적용 (전략 패턴)
     * 4. 결과 객체 생성
     * 5. 리스너들에게 알림 (옵저버 패턴)
     * 6. 결과 반환
     *
     * [예외 처리]
     * - 잘못된 가격이 입력되면 IllegalArgumentException을 던집니다
     * - 호출하는 측(Controller)에서 이 예외를 처리해야 합니다
     *
     * @param originalPrice 원래 가격
     * @param country 국가 코드
     * @param isVip VIP 여부
     * @return 결제 처리 결과
     * @throws IllegalArgumentException 가격이 0 미만인 경우
     */
    public PaymentResult process(double originalPrice, String country, boolean isVip) {
        // 1. 입력값 검증 (방어적 프로그래밍)
        if (originalPrice < 0) {
            // 잘못된 입력에 대해 명확한 예외를 던짐
            throw new IllegalArgumentException("잘못된 가격");
        }

        // 2. 할인 정책 적용 (전략 패턴)
        // discountPolicy는 인터페이스 타입이므로
        // 실제로 어떤 구현체가 들어있는지 이 코드는 모릅니다
        double discounted = discountPolicy.apply(originalPrice, isVip);

        // 3. 세금 정책 적용 (전략 패턴)
        // 마찬가지로 taxPolicy도 인터페이스 타입입니다
        double taxed = taxPolicy.apply(discounted);

        // 4. 결과 객체 생성
        // Record를 사용하여 간결하게 생성
        PaymentResult result = new PaymentResult(
                originalPrice, discounted, taxed, country, isVip
        );

        // 5. 모든 리스너에게 결제 완료 알림 (옵저버 패턴)
        // 향상된 for문 (Enhanced for loop)으로 리스너들을 순회
        for (PaymentListener listener : listeners) {
            listener.onPaymentCompleted(result);
        }

        // 6. 결과 반환
        return result;
    }
}
