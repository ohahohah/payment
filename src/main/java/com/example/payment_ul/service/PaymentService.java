package com.example.payment_ul.service;

import com.example.payment_ul.dto.PaymentRequest;
import com.example.payment_ul.dto.PaymentResult;
import com.example.payment_ul.entity.Payment;
import com.example.payment_ul.entity.PaymentStatus;
import com.example.payment_ul.handler.PaymentCompletionHandler;
import com.example.payment_ul.policy.discount.CustomerDiscountPolicy;
import com.example.payment_ul.policy.tax.TaxPolicy;
import com.example.payment_ul.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ====================================================================
 * PaymentService - 결제 서비스 (유비쿼터스 랭귀지 적용)
 * ====================================================================
 *
 * [독립 패키지]
 * - payment_ul 패키지 전용 Service입니다
 * - Qualifier 없이 독립적으로 동작합니다
 * - 로컬 정책(payment_ul.policy)을 사용합니다
 *
 * [메서드명 변경]
 * | 변경 전         | 변경 후               |
 * |-----------------|----------------------|
 * | execute()       | processPayment()     |
 * | getData()       | getPayment()         |
 * | getList()       | getAllPayments()     |
 * | updateStatus()  | refundPayment()      |
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

        if (request.originalPrice() < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다");
        }

        double discountedAmount = customerDiscountPolicy.apply(request.originalPrice(), request.isVip());
        double taxedAmount = taxPolicy.apply(discountedAmount);

        PaymentResult result = new PaymentResult(
                request.originalPrice(),
                discountedAmount,
                taxedAmount,
                request.country(),
                request.isVip()
        );

        Payment payment = Payment.create(
                result.originalPrice(),
                result.discountedAmount(),
                result.taxedAmount(),
                result.country(),
                result.isVip()
        );

        Payment saved = paymentRepository.save(payment);
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

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 결제만 환불할 수 있습니다");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(LocalDateTime.now());

        log.info("결제 환불 완료: id={}", id);
        return payment;
    }

    @Transactional(readOnly = true)
    public Double getTotalAmount(String country) {
        return paymentRepository.sumTaxedAmountByStatus(PaymentStatus.COMPLETED);
    }

    @Transactional(readOnly = true)
    public List<Payment> getRecentPayments(int limit) {
        return paymentRepository.findRecentPayments(limit);
    }
}
