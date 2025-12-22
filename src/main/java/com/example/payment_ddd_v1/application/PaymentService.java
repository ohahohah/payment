package com.example.payment_ddd_v1.application;

import com.example.payment_ddd_v1.domain.model.Country;
import com.example.payment_ddd_v1.domain.model.Money;
import com.example.payment_ddd_v1.domain.model.Payment;
import com.example.payment_ddd_v1.domain.policy.DiscountPolicy;
import com.example.payment_ddd_v1.domain.policy.TaxPolicy;
import com.example.payment_ddd_v1.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PaymentService - 결제 애플리케이션 서비스
 *
 * [Application Service 역할]
 * - 유스케이스 조율 (Orchestration)
 * - 트랜잭션 관리
 * - 도메인 객체 조합
 *
 * [주의]
 * - 비즈니스 로직은 Domain에 위임
 * - 여기서는 흐름만 제어
 */
@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DiscountPolicy discountPolicy;
    private final TaxPolicy taxPolicy;

    public PaymentService(PaymentRepository paymentRepository,
                          DiscountPolicy discountPolicy,
                          TaxPolicy taxPolicy) {
        this.paymentRepository = paymentRepository;
        this.discountPolicy = discountPolicy;
        this.taxPolicy = taxPolicy;
    }

    /**
     * 결제 생성
     */
    public Payment createPayment(double amount, String countryCode, boolean isVip) {
        Money originalPrice = Money.of(amount);
        Money discountedAmount = discountPolicy.apply(originalPrice, isVip);
        Money taxedAmount = taxPolicy.apply(discountedAmount);
        Country country = Country.of(countryCode);

        Payment payment = Payment.create(
                originalPrice,
                discountedAmount,
                taxedAmount,
                country,
                isVip
        );

        payment.complete();

        return paymentRepository.save(payment);
    }

    /**
     * 결제 조회
     */
    @Transactional(readOnly = true)
    public Payment getPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다: " + id));
    }

    /**
     * 전체 결제 조회
     */
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * 환불 처리
     */
    public Payment refundPayment(Long id) {
        Payment payment = getPayment(id);
        payment.refund();  // 비즈니스 로직은 도메인에서 처리
        return paymentRepository.save(payment);
    }
}
