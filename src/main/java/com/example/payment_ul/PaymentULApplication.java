package com.example.payment_ul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ====================================================================
 * PaymentULApplication - 유비쿼터스 랭귀지 결제 시스템 애플리케이션
 * ====================================================================
 *
 * [독립 실행 가능]
 * - 이 애플리케이션은 com.example.payment_ul 패키지만 스캔합니다
 * - 다른 패키지(payment, payment_ddd)와 독립적으로 실행됩니다
 *
 * [실행 방법]
 * ./gradlew bootRun -PmainClass=com.example.payment_ul.PaymentULApplication
 *
 * [유비쿼터스 랭귀지 적용]
 * - 모든 필드명, 메서드명이 도메인 용어를 사용합니다
 * - originalPrice, discountedAmount, taxedAmount
 * - PENDING, COMPLETED, REFUNDED
 */
@SpringBootApplication
public class PaymentULApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentULApplication.class, args);
    }
}
