package com.example.payment_step2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PaymentStep1Application - 독립 실행 가능한 Spring Boot 애플리케이션
 *
 * [payment_ul에서 변경된 점]
 * - Value Object 사용 (Money, Country)
 * - JPA @Convert로 Value Object 매핑
 * - Entity에서 비즈니스 메서드로 상태 변경
 */
@SpringBootApplication
public class PaymentStep2Application {

    public static void main(String[] args) {
        SpringApplication.run(PaymentStep2Application.class, args);
    }
}
