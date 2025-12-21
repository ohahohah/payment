package com.example.payment_ddd.infrastructure.config;

import com.example.payment_ddd.application.eventhandler.*;
import com.example.payment_ddd.application.service.PaymentCommandService;
import com.example.payment_ddd.domain.policy.*;
import com.example.payment_ddd.domain.repository.PaymentRepository;
import com.example.payment_ddd.domain.service.PaymentDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * PaymentDddConfig - DDD 결제 시스템 설정
 *
 * [Dependency Injection 설정]
 * - 도메인 객체들은 Spring 어노테이션 없이 순수 Java
 * - Infrastructure에서 Bean 등록 담당
 *
 * [도메인 순수성 유지]
 * - 도메인 레이어에 @Component, @Service 없음
 * - 프레임워크 독립적인 도메인
 */
@Configuration
public class PaymentDddConfig {

    /**
     * 할인 정책
     */
    @Bean
    public DiscountPolicy discountPolicy() {
        return new VipDiscountPolicy();
    }

    /**
     * 세금 정책 목록
     */
    @Bean
    public List<TaxPolicy> taxPolicies() {
        return List.of(
                new KoreaTaxPolicy(),
                new UsTaxPolicy()
        );
    }

    /**
     * 도메인 서비스
     */
    @Bean
    public PaymentDomainService paymentDomainService(DiscountPolicy discountPolicy,
                                                      List<TaxPolicy> taxPolicies) {
        return new PaymentDomainService(discountPolicy, taxPolicies);
    }

    /**
     * 이벤트 핸들러 목록
     */
    @Bean
    public List<DomainEventHandler<?>> domainEventHandlers() {
        return List.of(
                new LoggingEventHandler(),
                new SettlementEventHandler(),
                new RefundLoggingEventHandler()
        );
    }

    /**
     * 애플리케이션 서비스
     */
    @Bean
    public PaymentCommandService paymentCommandService(PaymentDomainService paymentDomainService,
                                                        PaymentRepository paymentRepository,
                                                        List<DomainEventHandler<?>> eventHandlers) {
        return new PaymentCommandService(paymentDomainService, paymentRepository, eventHandlers);
    }
}
