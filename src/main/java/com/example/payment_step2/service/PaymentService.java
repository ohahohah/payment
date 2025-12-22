package com.example.payment_step2.service;

import com.example.payment_step2.domain.model.Country;
import com.example.payment_step2.domain.model.Money;
import com.example.payment_step2.dto.PaymentRequest;
import com.example.payment_step2.dto.PaymentResult;
import com.example.payment_step2.entity.Payment;
import com.example.payment_step2.entity.PaymentStatus;
import com.example.payment_step2.handler.PaymentCompletionHandler;
import com.example.payment_step2.policy.discount.CustomerDiscountPolicy;
import com.example.payment_step2.policy.tax.TaxPolicy;
import com.example.payment_step2.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PaymentService - 결제 서비스
 *
 * ============================================================================
 * [payment_ul에서 변경된 점 - Value Object만 적용]
 * ============================================================================
 *
 * 1. Value Object 사용:
 *    - double -> Money
 *    - String -> Country
 *
 * 2. 수동 검증 제거:
 *    변경 전:
 *      if (request.originalPrice() < 0) {
 *          throw new IllegalArgumentException("가격은 0 이상이어야 합니다");
 *      }
 *
 *    변경 후:
 *      Money.of(request.originalPrice())  // 자동 검증
 *
 * 3. Policy 반환 타입:
 *    변경 전: double discountedAmount = policy.apply(double, boolean)
 *    변경 후: Money discountedAmount = policy.apply(Money, boolean)
 *
 * [변경되지 않은 점]
 * - Entity 상태 변경은 payment_ul과 동일하게 setter 사용
 * - 비즈니스 로직(상태 검증)은 Service에서 처리
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final CustomerDiscountPolicy customerDiscountPolicy;
    private final TaxPolicy taxPolicy;
    private final List<PaymentCompletionHandler> completionHandlers;

    public PaymentService(PaymentRepository paymentRepository,
                          CustomerDiscountPolicy customerDiscountPolicy,
                          TaxPolicy taxPolicy,
                          List<PaymentCompletionHandler> completionHandlers) {
        this.paymentRepository = paymentRepository;
        this.customerDiscountPolicy = customerDiscountPolicy;
        this.taxPolicy = taxPolicy;
        this.completionHandlers = completionHandlers;
    }

    @Transactional
    public PaymentResult processPayment(PaymentRequest request) {
        log.debug("결제 처리 시작: originalPrice={}, country={}, isVip={}",
                request.originalPrice(), request.country(), request.isVip());

        // [변경] Value Object 생성 - 자동 검증
        // Money.of()에서 음수면 예외 발생 -> 별도 if문 불필요
        // Country.of()에서 지원하지 않는 국가면 예외 발생
        Money originalPrice = Money.of(request.originalPrice());
        Country country = Country.of(request.country());

        // [변경] Policy가 Money를 받고 Money를 반환
        Money discountedAmount = customerDiscountPolicy.apply(originalPrice, request.isVip());
        Money taxedAmount = taxPolicy.apply(discountedAmount);

        // [변경] DTO는 여전히 primitive 타입 (외부 API용)
        // Money.getAmount(), Country.getCode()로 변환
        PaymentResult result = new PaymentResult(
                originalPrice.getAmount(),
                discountedAmount.getAmount(),
                taxedAmount.getAmount(),
                country.getCode(),
                request.isVip()
        );

        // [변경] Entity 생성 시 Value Object 전달
        Payment payment = Payment.create(
                originalPrice,
                discountedAmount,
                taxedAmount,
                country,
                request.isVip()
        );

        Payment saved = paymentRepository.save(payment);

        // [유지] payment_ul과 동일하게 setter로 상태 변경
        saved.setStatus(PaymentStatus.COMPLETED);
        saved.setUpdatedAt(LocalDateTime.now());

        log.info("결제 처리 완료: id={}, taxedAmount={}", saved.getId(), result.taxedAmount());

        for (PaymentCompletionHandler handler : completionHandlers) {
            handler.onPaymentCompleted(result);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Payment getPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다: " + id));
    }

    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    @Transactional
    public Payment refundPayment(Long id) {
        Payment payment = getPayment(id);

        // [유지] payment_ul과 동일하게 Service에서 상태 검증
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 결제만 환불할 수 있습니다");
        }

        // [유지] setter로 상태 변경
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(LocalDateTime.now());

        log.info("결제 환불 완료: id={}", id);
        return payment;
    }

    @Transactional(readOnly = true)
    public Double getTotalAmount(String countryCode) {
        return paymentRepository.sumTaxedAmountByStatus(PaymentStatus.COMPLETED);
    }

    @Transactional(readOnly = true)
    public List<Payment> getRecentPayments(int limit) {
        return paymentRepository.findRecentPayments(limit);
    }
}
