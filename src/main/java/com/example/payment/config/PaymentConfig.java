package com.example.payment.config;

import org.springframework.context.annotation.Configuration;

/**
 * ====================================================================
 * PaymentConfig - 스프링 빈(Bean) 설정 클래스
 * ====================================================================
 *
 * [@Configuration 어노테이션]
 * - 이 클래스가 스프링 빈 설정을 담당하는 클래스임을 나타냅니다
 * - 필요한 경우 @Bean 메서드를 추가하여 빈을 등록할 수 있습니다
 *
 * [Spring Boot의 컴포넌트 스캔 방식]
 * - Spring Boot에서는 @Component, @Service, @Repository 어노테이션을 사용하여
 *   클래스를 빈으로 자동 등록하는 것을 권장합니다
 * - @Bean을 사용한 수동 등록은 외부 라이브러리 클래스에만 사용합니다
 *
 * [현재 빈 등록 방식]
 * - DiscountPolicy: DefaultDiscountPolicy에 @Component @Primary
 * - TaxPolicy: KoreaTaxPolicy에 @Component @Primary, UsTaxPolicy에 @Component
 * - PaymentListener: LoggingListener, SettlementListener에 @Component
 * - PaymentProcessor, PaymentService: @Service
 * - PaymentController: @RestController
 * - PaymentRepository: @Repository (Spring Data JPA 자동)
 *
 * [List<T> 자동 수집]
 * - 스프링은 List<PaymentListener> 타입 주입 시
 *   PaymentListener 타입의 모든 빈을 자동으로 수집합니다
 * - 명시적으로 빈을 등록할 필요가 없습니다
 *
 * [왜 @Bean 메서드가 없어졌나요?]
 * - 이전에는 PaymentConfig에서 @Bean으로 모든 빈을 수동 등록했습니다
 * - Spring Boot 권장 방식인 @Component 스캔으로 변경했습니다
 * - 각 클래스에 직접 @Component를 붙이면:
 *   1. 해당 클래스 파일만 보면 빈인지 알 수 있음 (가독성)
 *   2. Config 클래스가 비대해지지 않음 (유지보수성)
 *   3. 빈 등록과 클래스 정의가 분리되지 않음 (응집도)
 */
@Configuration
public class PaymentConfig {
    // 현재 모든 빈이 @Component 스캔으로 자동 등록되므로
    // 추가적인 @Bean 정의가 필요하지 않습니다.
    //
    // 외부 라이브러리 클래스를 빈으로 등록해야 할 때만
    // 여기에 @Bean 메서드를 추가하세요.
    //
    // 예시:
    // @Bean
    // public ObjectMapper objectMapper() {
    //     return new ObjectMapper()
    //         .registerModule(new JavaTimeModule())
    //         .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    // }
}
