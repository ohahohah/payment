package com.example.payment_step2_2.service;

import com.example.payment_step2_2.domain.model.Country;
import com.example.payment_step2_2.domain.model.Money;
import com.example.payment_step2_2.domain.policy.DiscountPolicy;
import com.example.payment_step2_2.domain.policy.TaxPolicy;
import com.example.payment_step2_2.dto.PaymentRequest;
import com.example.payment_step2_2.entity.Payment;
import com.example.payment_step2_2.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PaymentService - 결제 서비스
 *
 * ============================================================================
 * [payment_step1에서 변경된 점 - Aggregate 패턴 적용]
 * ============================================================================
 *
 * 상태 변경 로직이 Service에서 Entity로 이동:
 *
 * [변경 전 - payment_step1]
 * // completePayment()
 * payment.setStatus(PaymentStatus.COMPLETED);
 * payment.setUpdatedAt(LocalDateTime.now());
 *
 * // refundPayment()
 * if (payment.getStatus() != PaymentStatus.COMPLETED) {
 *     throw new IllegalStateException("완료된 결제만 환불할 수 있습니다");
 * }
 * payment.setStatus(PaymentStatus.REFUNDED);
 * payment.setUpdatedAt(LocalDateTime.now());
 *
 * [변경 후 - payment_step2_2]
 * // completePayment()
 * payment.complete();  // Entity 내부에서 검증 + 상태 변경
 *
 * // refundPayment()
 * payment.refund();    // Entity 내부에서 검증 + 상태 변경
 *
 * ============================================================================
 * [장점]
 * ============================================================================
 * 1. Service 코드 단순화 - 비즈니스 로직이 없어짐
 * 2. 상태 전이 규칙 중앙화 - Payment Entity에서 일관성 보장
 * 3. 테스트 용이성 - Entity 단위 테스트로 규칙 검증
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
    public Payment createPayment(PaymentRequest request) {
        Money originalPrice = Money.of(request.getPrice());
        Country country = Country.of(request.getCountryCode());
        Boolean isVip = request.getIsVip();

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
     * 결제 완료 처리
     *
     * [변경됨] setter 대신 비즈니스 메서드 사용
     *
     * payment_step1:
     *   payment.setStatus(PaymentStatus.COMPLETED);
     *   payment.setUpdatedAt(LocalDateTime.now());
     *
     * payment_step2_2:
     *   payment.complete();  // 검증 + 상태 변경이 Entity 안에!
     */
    public Payment completePayment(Long id) {
        Payment payment = getPayment(id);
        payment.complete();  // [변경] setter 대신 비즈니스 메서드
        return payment;
    }

    /**
     * 환불 처리
     *
     * [변경됨] 상태 검증 로직이 Entity로 이동
     *
     * payment_step1:
     *   if (payment.getStatus() != PaymentStatus.COMPLETED) {
     *       throw new IllegalStateException("완료된 결제만 환불할 수 있습니다");
     *   }
     *   payment.setStatus(PaymentStatus.REFUNDED);
     *   payment.setUpdatedAt(LocalDateTime.now());
     *
     * payment_step2_2:
     *   payment.refund();  // 검증 로직이 Entity 안에!
     */
    public Payment refundPayment(Long id) {
        Payment payment = getPayment(id);
        payment.refund();  // [변경] 검증 + 상태 변경이 Entity 안에
        return payment;
    }

    /**
     * 결제 실패 처리
     *
     * [변경됨] setter 대신 비즈니스 메서드 사용
     */
    public Payment failPayment(Long id) {
        Payment payment = getPayment(id);
        payment.fail();  // [변경] setter 대신 비즈니스 메서드
        return payment;
    }
}
