package com.example.payment.service;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResult;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.listener.PaymentListener;
import com.example.payment.policy.discount.DiscountPolicy;
import com.example.payment.policy.tax.TaxPolicy;
import com.example.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ====================================================================
 * PaymentService - 결제 서비스 (모든 비즈니스 로직 처리)
 * ====================================================================
 *
 * [현재 구조]
 * - 트랜잭션 관리, 비즈니스 로직, 도메인 로직이 모두 이 클래스에 위치
 * - 할인 계산, 세금 계산, 상태 변경 등 모든 로직을 직접 처리
 *
 * [Service 계층의 역할]
 * 1. 트랜잭션 관리: @Transactional로 트랜잭션 경계 설정
 * 2. 비즈니스 로직: 할인/세금 계산, 상태 변경
 * 3. 데이터 접근: Repository를 통한 DB 작업
 * 4. 이벤트 처리: 결제 완료 시 리스너 호출
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final DiscountPolicy discountPolicy;
    private final TaxPolicy taxPolicy;
    private final List<PaymentListener> listeners;

    /**
     * 생성자 주입
     *
     * @param paymentRepository 결제 저장소
     * @param discountPolicy 할인 정책
     * @param taxPolicy 세금 정책
     * @param listeners 결제 리스너 목록
     */
    public PaymentService(PaymentRepository paymentRepository,
                          DiscountPolicy discountPolicy,
                          TaxPolicy taxPolicy,
                          List<PaymentListener> listeners) {
        this.paymentRepository = paymentRepository;
        this.discountPolicy = discountPolicy;
        this.taxPolicy = taxPolicy;
        this.listeners = listeners;
    }

    /**
     * 결제 처리 및 저장
     *
     * [처리 순서]
     * 1. 입력값 검증
     * 2. 할인 정책 적용
     * 3. 세금 정책 적용
     * 4. 엔티티 생성 및 저장
     * 5. 결제 완료 처리 (상태 변경)
     * 6. 리스너 알림
     *
     * @param request 결제 요청 정보
     * @return 결제 결과
     */
    @Transactional
    public PaymentResult processPayment(PaymentRequest request) {
        log.debug("결제 처리 시작: originalPrice={}, country={}, isVip={}",
                request.originalPrice(), request.country(), request.isVip());

        // 1. 입력값 검증
        if (request.originalPrice() < 0) {
            throw new IllegalArgumentException("잘못된 가격");
        }

        // 2. 할인 정책 적용
        double discounted = discountPolicy.apply(request.originalPrice(), request.isVip());

        // 3. 세금 정책 적용
        double taxed = taxPolicy.apply(discounted);

        // 4. 결과 객체 생성
        PaymentResult result = new PaymentResult(
                request.originalPrice(), discounted, taxed,
                request.country(), request.isVip()
        );

        // 5. 엔티티 생성
        Payment payment = Payment.create(
                result.originalPrice(),
                result.discountedAmount(),
                result.taxedAmount(),
                result.country(),
                result.isVip()
        );

        // 6. DB에 저장 (PENDING 상태)
        Payment savedPayment = paymentRepository.save(payment);
        log.debug("결제 저장 완료: id={}", savedPayment.getId());

        // 7. 결제 완료 처리 (서비스에서 직접 상태 변경)
        savedPayment.setStatus(PaymentStatus.COMPLETED);
        savedPayment.setUpdatedAt(LocalDateTime.now());
        log.info("결제 완료: id={}, amount={}", savedPayment.getId(), result.taxedAmount());

        // 8. 리스너들에게 알림
        for (PaymentListener listener : listeners) {
            listener.onPaymentCompleted(result);
        }

        return result;
    }

    /**
     * 결제 ID로 조회
     *
     * @param id 결제 ID
     * @return 결제 엔티티 (없으면 예외)
     * @throws IllegalArgumentException 결제를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Payment getPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다: " + id));
    }

    /**
     * 전체 결제 목록 조회
     *
     * @return 전체 결제 목록
     */
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * 상태별 결제 목록 조회
     *
     * @param status 조회할 상태
     * @return 해당 상태의 결제 목록
     */
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    /**
     * 결제 환불 처리
     *
     * [서비스에서 직접 상태 검증 및 변경]
     * - 환불 가능 여부를 서비스에서 판단
     * - 엔티티의 setter를 호출하여 상태 변경
     *
     * @param id 환불할 결제 ID
     * @return 환불 처리된 결제
     */
    @Transactional
    public Payment refundPayment(Long id) {
        Payment payment = getPayment(id);

        // 환불 가능 여부 검증 (서비스에서 직접 처리)
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 결제만 환불할 수 있습니다.");
        }

        // 상태 변경 (서비스에서 직접 처리)
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(LocalDateTime.now());

        log.info("결제 환불 완료: id={}", id);
        return payment;
    }

    /**
     * 국가별 결제 총액 조회
     *
     * @param country 국가 코드
     * @return 완료된 결제의 총액
     */
    @Transactional(readOnly = true)
    public Double getTotalAmountByCountry(String country) {
        return paymentRepository.sumTaxedAmountByStatus(PaymentStatus.COMPLETED);
    }

    /**
     * 최근 결제 목록 조회
     *
     * @param limit 조회할 건수
     * @return 최근 결제 목록
     */
    @Transactional(readOnly = true)
    public List<Payment> getRecentPayments(int limit) {
        return paymentRepository.findRecentPayments(limit);
    }
}
