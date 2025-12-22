package com.example.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PaymentApplication - 결제 시스템 애플리케이션
 *
 * [독립 실행 가능]
 * - 이 애플리케이션은 com.example.payment 패키지만 스캔합니다
 * - 다른 패키지(payment_ul, payment_ddd)와 독립적으로 실행됩니다
 *
 * [실행 방법]
 * ./gradlew bootRun -PmainClass=com.example.payment.PaymentApplication
 */
@SpringBootApplication
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
