package com.example.payment_ddd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ====================================================================
 * PaymentDddApplication - DDD 결제 시스템 애플리케이션
 * ====================================================================
 *
 * [독립 실행 가능]
 * - 이 애플리케이션은 com.example.payment_ddd 패키지만 스캔합니다
 * - 다른 패키지(payment, payment_ul)와 독립적으로 실행됩니다
 *
 * [실행 방법]
 * ./gradlew bootRun -PmainClass=com.example.payment_ddd.PaymentDddApplication
 *
 * [DDD 아키텍처]
 * ├── domain/          도메인 레이어 (핵심 비즈니스 로직)
 * │   ├── model/       Aggregate, Entity, Value Object
 * │   ├── event/       도메인 이벤트
 * │   ├── policy/      도메인 정책
 * │   ├── repository/  Repository 인터페이스
 * │   └── service/     도메인 서비스
 * ├── application/     애플리케이션 레이어
 * │   ├── command/     Command 객체
 * │   ├── service/     애플리케이션 서비스
 * │   └── eventhandler/ 이벤트 핸들러
 * ├── infrastructure/  인프라 레이어
 * │   ├── persistence/ JPA 구현
 * │   └── config/      설정
 * └── interfaces/      인터페이스 레이어
 *     ├── dto/         DTO
 *     └── rest/        REST Controller
 */
@SpringBootApplication
public class PaymentDddApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentDddApplication.class, args);
    }
}
