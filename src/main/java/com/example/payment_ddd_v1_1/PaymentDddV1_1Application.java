package com.example.payment_ddd_v1_1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PaymentDddV1_1Application - 정석 DDD 계층형 아키텍처
 *
 * ============================================================================
 * [payment_ddd_v1과의 차이점]
 * ============================================================================
 *
 * payment_ddd_v1 (실용적):
 *   Domain의 Payment.java에 @Entity 어노테이션
 *   → JPA 의존이 Domain에 침투
 *
 * payment_ddd_v1_1 (정석):
 *   Domain의 Payment.java는 순수 Java
 *   Infrastructure의 PaymentJpaEntity.java에 @Entity
 *   PaymentMapper로 변환
 *
 * ============================================================================
 * [패키지 구조]
 * ============================================================================
 *
 * payment_ddd_v1_1/
 * ├── interfaces/                     # 사용자 인터페이스 계층
 * │   ├── PaymentController.java
 * │   ├── PaymentRequest.java
 * │   └── PaymentResponse.java
 * │
 * ├── application/                    # 응용 서비스 계층
 * │   └── PaymentService.java
 * │
 * ├── domain/                         # 도메인 계층 (순수 Java!)
 * │   ├── model/
 * │   │   ├── Payment.java           # @Entity 없음!
 * │   │   ├── PaymentStatus.java
 * │   │   ├── Money.java
 * │   │   └── Country.java
 * │   ├── policy/
 * │   │   └── DiscountPolicy.java, TaxPolicy.java
 * │   └── repository/
 * │       └── PaymentRepository.java  # 인터페이스
 * │
 * └── infrastructure/                 # 인프라 계층 (JPA 의존!)
 *     ├── persistence/
 *     │   ├── PaymentJpaEntity.java  # @Entity 있음!
 *     │   ├── JpaPaymentRepository.java
 *     │   └── SpringDataPaymentRepository.java
 *     └── mapper/
 *         └── PaymentMapper.java     # Domain ↔ JPA 변환
 *
 * ============================================================================
 * [실행 방법]
 * ============================================================================
 *
 * ./gradlew bootRun -PmainClass=com.example.payment_ddd_v1_1.PaymentDddV1_1Application
 *
 * API: http://localhost:8080/api/ddd/v1_1/payments
 */
@SpringBootApplication
public class PaymentDddV1_1Application {

    public static void main(String[] args) {
        SpringApplication.run(PaymentDddV1_1Application.class, args);
    }
}
