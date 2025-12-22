package com.example.payment_step2_2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PaymentStep2_2Application - Aggregate 패턴 적용
 *
 * [payment_step1에서 변경된 점]
 * - Payment가 Aggregate Root로 변경
 * - 비즈니스 메서드로 상태 변경 (complete, refund, fail)
 * - setter 제거
 * - 상태 전이 규칙이 Entity 안에 캡슐화
 */
@SpringBootApplication
public class PaymentStep2_2Application {

    public static void main(String[] args) {
        SpringApplication.run(PaymentStep2_2Application.class, args);
    }
}
