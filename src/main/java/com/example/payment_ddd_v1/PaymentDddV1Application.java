package com.example.payment_ddd_v1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PaymentDddV1Application - DDD 계층형 아키텍처 예제
 *
 * ============================================================================
 * [프로젝트 개요]
 * ============================================================================
 *
 * DDD(Domain-Driven Design) 계층형 아키텍처를 적용한 결제 시스템
 * - Rich Domain Model (비즈니스 로직이 Entity에 캡슐화)
 * - Value Object (Money, Country)
 * - Repository 패턴 (의존성 역전)
 * - JPA를 활용한 영속화
 *
 * ============================================================================
 * [계층 구조]
 * ============================================================================
 *
 * interfaces/          - 사용자 인터페이스 계층 (Controller, DTO)
 *     ↓ 의존
 * application/         - 응용 서비스 계층 (PaymentService)
 *     ↓ 의존
 * domain/              - 도메인 계층 (Entity, Value Object, Repository 인터페이스)
 *     ↑ 구현
 * infrastructure/      - 인프라 계층 (JPA Repository 구현체, Converter)
 *
 * ============================================================================
 * [패키지 구조]
 * ============================================================================
 *
 * payment_ddd_v1/
 * ├── interfaces/                    # 사용자 인터페이스 계층
 * │   ├── PaymentController.java     # REST API 컨트롤러
 * │   └── PaymentDto.java            # 요청/응답 DTO
 * │
 * ├── application/                   # 응용 서비스 계층
 * │   └── PaymentService.java        # 유스케이스 조율 (비즈니스 로직 없음)
 * │
 * ├── domain/                        # 도메인 계층 (핵심!)
 * │   ├── model/
 * │   │   ├── Payment.java           # Aggregate Root (Rich Domain Model)
 * │   │   ├── PaymentStatus.java     # 상태 열거형
 * │   │   ├── Money.java             # Value Object
 * │   │   └── Country.java           # Value Object
 * │   ├── policy/
 * │   │   ├── DiscountPolicy.java    # 할인 정책 인터페이스
 * │   │   └── TaxPolicy.java         # 세금 정책 인터페이스
 * │   └── repository/
 * │       └── PaymentRepository.java # Repository 인터페이스 (DIP)
 * │
 * └── infrastructure/                # 인프라 계층
 *     ├── JpaPaymentRepository.java  # Repository 구현체
 *     └── converter/
 *         ├── MoneyConverter.java    # Money -> DB 변환
 *         └── CountryConverter.java  # Country -> DB 변환
 *
 * ============================================================================
 * [실행 방법]
 * ============================================================================
 *
 * # payment-processor 디렉토리에서
 * ./gradlew bootRun -PmainClass=com.example.payment_ddd_v1.PaymentDddV1Application
 *
 * # H2 Console 접속
 * http://localhost:8080/h2-console
 * JDBC URL: jdbc:h2:mem:paymentdb
 *
 * # API 테스트
 * POST http://localhost:8080/api/ddd/v1/payments
 * GET  http://localhost:8080/api/ddd/v1/payments/{id}
 */
@SpringBootApplication
public class PaymentDddV1Application {

    public static void main(String[] args) {
        SpringApplication.run(PaymentDddV1Application.class, args);
    }
}
