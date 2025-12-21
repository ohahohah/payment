package com.example.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * ====================================================================
 * PaymentApplication - 스프링 부트 애플리케이션의 시작점 (Entry Point)
 * ====================================================================
 *
 * [스프링 부트 기초 개념]
 * 스프링 부트는 스프링 프레임워크를 쉽게 사용할 수 있게 해주는 프레임워크입니다.
 * 복잡한 XML 설정 없이 어노테이션(@) 기반으로 설정할 수 있습니다.
 *
 * [@SpringBootApplication 어노테이션]
 * 이 어노테이션은 아래 3개의 어노테이션을 합친 것입니다:
 *
 * 1. @SpringBootConfiguration
 *    - 이 클래스가 스프링 부트 설정 클래스임을 나타냅니다
 *
 * 2. @EnableAutoConfiguration
 *    - 스프링 부트가 자동으로 필요한 빈(Bean)들을 설정하도록 합니다
 *    - 예: 웹 서버, 데이터베이스 연결 등을 자동 설정
 *
 * 3. @ComponentScan
 *    - 이 패키지(com.example.payment)와 하위 패키지에서
 *    - @Component, @Service, @Controller 등이 붙은 클래스를 자동으로 찾아
 *    - 스프링 빈(Bean)으로 등록합니다
 *
 * [빈(Bean)이란?]
 * 스프링이 관리하는 객체입니다. 개발자가 직접 new로 생성하지 않고
 * 스프링이 대신 생성하고 관리합니다. 이를 IoC(제어의 역전)라고 합니다.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.payment", "com.example.payment_ddd"})
@EntityScan(basePackages = {"com.example.payment", "com.example.payment_ddd"})
@EnableJpaRepositories(basePackages = {"com.example.payment", "com.example.payment_ddd"})
public class PaymentApplication {

    /**
     * main 메서드 - 자바 애플리케이션의 시작점
     *
     * SpringApplication.run() 메서드가 하는 일:
     * 1. 스프링 컨테이너(ApplicationContext)를 생성
     * 2. @ComponentScan으로 빈들을 찾아서 등록
     * 3. 내장 톰캣 웹 서버를 시작 (기본 포트: 8080)
     * 4. 애플리케이션 준비 완료
     *
     * @param args 커맨드라인 인자 (실행 시 전달되는 파라미터)
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
