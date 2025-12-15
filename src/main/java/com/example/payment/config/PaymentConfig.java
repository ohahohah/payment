package com.example.payment.config;

import com.example.payment.listener.LoggingListener;
import com.example.payment.listener.PaymentListener;
import com.example.payment.listener.SettlementListener;
import com.example.payment.policy.discount.DefaultDiscountPolicy;
import com.example.payment.policy.discount.DiscountPolicy;
import com.example.payment.policy.tax.KoreaTaxPolicy;
import com.example.payment.policy.tax.TaxPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * ====================================================================
 * PaymentConfig - 스프링 빈(Bean) 설정 클래스
 * ====================================================================
 *
 * [@Configuration 어노테이션]
 * - 이 클래스가 스프링 빈 설정을 담당하는 클래스임을 나타냅니다
 * - 이 클래스 안의 @Bean 메서드들이 반환하는 객체들이 스프링 빈으로 등록됩니다
 * - XML 설정 파일의 <beans> 태그와 같은 역할입니다
 *
 * [왜 Configuration 클래스를 사용하나요?]
 * - @Component를 붙일 수 없는 외부 라이브러리 클래스를 빈으로 등록할 때
 * - 빈 생성 시 복잡한 로직이 필요할 때
 * - 조건에 따라 다른 빈을 등록해야 할 때
 *
 * [이 프로젝트에서의 역할]
 * - DiscountPolicy, TaxPolicy 등 전략 패턴의 구현체들을 빈으로 등록
 * - PaymentListener 구현체들을 빈으로 등록
 */
@Configuration
public class PaymentConfig {

    /**
     * [@Bean 어노테이션]
     * - 이 메서드가 반환하는 객체를 스프링 빈으로 등록합니다
     * - 메서드 이름(defaultDiscountPolicy)이 빈의 이름이 됩니다
     * - 스프링 컨테이너가 이 메서드를 호출하여 빈을 생성합니다
     *
     * [@Primary 어노테이션]
     * - 같은 타입의 빈이 여러 개 있을 때, 이 빈을 기본(우선)으로 사용합니다
     * - 예: DiscountPolicy 타입의 빈이 여러 개라면, @Primary가 붙은 것이 기본 주입됩니다
     *
     * [빈의 생명주기]
     * 1. 스프링 컨테이너 시작
     * 2. @Bean 메서드 호출하여 객체 생성
     * 3. 의존성 주입 (DI)
     * 4. 애플리케이션 실행 중 빈 사용
     * 5. 스프링 컨테이너 종료 시 빈 소멸
     */
    @Bean
    @Primary
    public DiscountPolicy defaultDiscountPolicy() {
        // new 키워드로 직접 객체를 생성하지만,
        // 스프링이 이 객체를 관리하게 됩니다
        return new DefaultDiscountPolicy();
    }

    /**
     * 한국 세금 정책 빈 등록
     *
     * @Primary가 붙어있으므로 TaxPolicy 타입이 필요할 때
     * 기본적으로 이 KoreaTaxPolicy가 주입됩니다
     */
    @Bean
    @Primary
    public TaxPolicy koreaTaxPolicy() {
        return new KoreaTaxPolicy();
    }

    /**
     * 로깅 리스너 빈 등록
     *
     * PaymentListener 타입의 빈이 여러 개 등록됩니다
     * (loggingListener, settlementListener)
     */
    @Bean
    public PaymentListener loggingListener() {
        return new LoggingListener();
    }

    /**
     * 정산 리스너 빈 등록
     */
    @Bean
    public PaymentListener settlementListener() {
        return new SettlementListener();
    }

    /**
     * [List 타입 주입의 특별한 기능]
     *
     * 스프링은 List<PaymentListener> 타입을 주입받으면
     * PaymentListener 타입의 모든 빈들을 자동으로 수집하여 리스트로 만들어줍니다
     *
     * 즉, loggingListener와 settlementListener가 모두 이 리스트에 포함됩니다
     *
     * 이 기능을 통해 새로운 Listener를 추가할 때
     * 기존 코드를 수정하지 않고 빈만 등록하면 됩니다 (개방-폐쇄 원칙, OCP)
     *
     * @param listeners 스프링이 자동으로 수집한 PaymentListener 빈들의 목록
     * @return PaymentListener 목록
     */
    @Bean
    public List<PaymentListener> paymentListeners(List<PaymentListener> listeners) {
        return listeners;
    }
}
