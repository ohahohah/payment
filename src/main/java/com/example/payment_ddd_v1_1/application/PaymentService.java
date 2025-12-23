package com.example.payment_ddd_v1_1.application;

import com.example.payment_ddd_v1_1.domain.model.Country;
import com.example.payment_ddd_v1_1.domain.model.Money;
import com.example.payment_ddd_v1_1.domain.model.Payment;
import com.example.payment_ddd_v1_1.domain.policy.DiscountPolicy;
import com.example.payment_ddd_v1_1.domain.policy.TaxPolicy;
import com.example.payment_ddd_v1_1.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PaymentService - 결제 응용 서비스
 *
 * [Application 계층]
 * - 유스케이스 조율
 * - 트랜잭션 관리
 * - 비즈니스 로직은 Domain에 위임
 */
@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DiscountPolicy customerDiscountPolicy;
    private final DiscountPolicy vipDiscountPolicy;
    private final TaxPolicy taxPolicy;

    public PaymentService(
            PaymentRepository paymentRepository,
            DiscountPolicy customerDiscountPolicy,
            DiscountPolicy vipDiscountPolicy,
            TaxPolicy taxPolicy) {
        this.paymentRepository = paymentRepository;
        this.customerDiscountPolicy = customerDiscountPolicy;
        this.vipDiscountPolicy = vipDiscountPolicy;
        this.taxPolicy = taxPolicy;
    }

    /**
     * 결제 생성
     */
    public Payment createPayment(double amount, String countryCode, boolean isVip) {
        Money originalPrice = Money.of(amount);
        Country country = Country.of(countryCode);

        DiscountPolicy discountPolicy = isVip ? vipDiscountPolicy : customerDiscountPolicy;
        Money discount = discountPolicy.calculateDiscount(originalPrice);
        Money discountedAmount = originalPrice.subtract(discount);
        Money taxedAmount = taxPolicy.applyTax(discountedAmount);

        Payment payment = Payment.create(
                originalPrice, discountedAmount, taxedAmount, country, isVip);

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
     * 결제 완료
     */
    public Payment completePayment(Long id) {
        Payment payment = getPayment(id);
        payment.complete();
        return paymentRepository.save(payment);
    }

    /**
     * 환불 처리
     */
    public Payment refundPayment(Long id) {
        Payment payment = getPayment(id);
        payment.refund();
        return paymentRepository.save(payment);
    }

    /**
     * 결제 실패
     */
    public Payment failPayment(Long id) {
        Payment payment = getPayment(id);
        payment.fail();
        return paymentRepository.save(payment);
    }
}
