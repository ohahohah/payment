package com.example.payment_ddd_v1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PaymentDddV1Application - DDD 레이어드 아키텍처 (단순화 버전)
 *
 * [실행 방법]
 * ./gradlew bootRun -PmainClass=com.example.payment_ddd_v1.PaymentDddV1Application
 */
@SpringBootApplication
public class PaymentDddV1Application {

    public static void main(String[] args) {
        SpringApplication.run(PaymentDddV1Application.class, args);
    }
}
