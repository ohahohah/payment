package com.example.payment.service;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResult;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ====================================================================
 * PaymentService - 결제 서비스 (트랜잭션 관리 + 비즈니스 로직 조합)
 * ====================================================================
 *
 * [Service 계층의 역할]
 * 1. 트랜잭션 관리: @Transactional로 트랜잭션 경계 설정
 * 2. 비즈니스 로직 조합: 여러 도메인 객체/서비스를 조합
 * 3. 외부 서비스 연동: 다른 시스템과의 통신
 *
 * [현업 구조]
 * Controller → Service → Repository
 *                ↓
 *           PaymentProcessor (비즈니스 로직)
 *
 * - Controller: HTTP 요청/응답 처리
 * - Service: 트랜잭션 관리, 비즈니스 흐름 조율
 * - Repository: 데이터 접근
 * - Processor/Domain: 순수 비즈니스 로직
 *
 * [@Service 어노테이션]
 * - 서비스 계층의 컴포넌트임을 나타냅니다
 * - @Component의 특수화된 형태
 * - 스프링이 빈으로 등록합니다
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentProcessor paymentProcessor;

    /**
     * 생성자 주입
     *
     * [의존성 주입 순서]
     * - PaymentRepository: 데이터 저장/조회
     * - PaymentProcessor: 결제 금액 계산 (할인, 세금)
     *
     * @param paymentRepository 결제 저장소
     * @param paymentProcessor 결제 처리기 (금액 계산)
     */
    public PaymentService(PaymentRepository paymentRepository,
                          PaymentProcessor paymentProcessor) {
        this.paymentRepository = paymentRepository;
        this.paymentProcessor = paymentProcessor;
    }

    /**
     * 결제 처리 및 저장
     *
     * [@Transactional 어노테이션]
     * - 이 메서드를 트랜잭션으로 묶습니다
     * - 메서드 시작 시 트랜잭션 시작
     * - 메서드 정상 종료 시 커밋 (COMMIT)
     * - 예외 발생 시 롤백 (ROLLBACK)
     *
     * [트랜잭션(Transaction)이란?]
     * - 데이터베이스 작업의 논리적 단위입니다
     * - ACID 속성:
     *   - Atomicity (원자성): 모두 성공하거나 모두 실패
     *   - Consistency (일관성): 트랜잭션 전후로 데이터 일관성 유지
     *   - Isolation (격리성): 동시 실행 트랜잭션 간 간섭 방지
     *   - Durability (영속성): 커밋된 데이터는 영구 저장
     *
     * [왜 Service에서 @Transactional을 사용하나요?]
     * - 여러 Repository 호출을 하나의 트랜잭션으로 묶기 위해
     * - 예: 결제 저장 + 포인트 적립 + 알림 발송을 한 트랜잭션으로
     *
     * @param request 결제 요청 정보
     * @return 결제 결과
     */
    @Transactional
    public PaymentResult processPayment(PaymentRequest request) {
        log.debug("결제 처리 시작: originalPrice={}, country={}, isVip={}",
                request.originalPrice(), request.country(), request.isVip());

        // 1. 비즈니스 로직 수행 (금액 계산)
        // - PaymentProcessor에 위임하여 할인/세금 계산
        PaymentResult result = paymentProcessor.process(
                request.originalPrice(),
                request.country(),
                request.isVip()
        );

        // 2. 엔티티 생성
        // - 정적 팩토리 메서드 사용
        Payment payment = Payment.create(
                result.originalPrice(),
                result.discountedAmount(),
                result.taxedAmount(),
                result.country(),
                result.isVip()
        );

        // 3. DB에 저장 (PENDING 상태)
        Payment savedPayment = paymentRepository.save(payment);
        log.debug("결제 저장 완료: id={}", savedPayment.getId());

        // 4. 결제 완료 처리
        // - 엔티티의 도메인 메서드 호출
        // - JPA 변경 감지(Dirty Checking)로 자동 UPDATE
        savedPayment.complete();
        log.info("결제 완료: id={}, amount={}", savedPayment.getId(), result.taxedAmount());

        return result;
    }

    /**
     * 결제 ID로 조회
     *
     * [@Transactional(readOnly = true)]
     * - 읽기 전용 트랜잭션을 설정합니다
     * - 성능 최적화: Hibernate가 변경 감지(Dirty Checking)를 수행하지 않음
     * - 명시적 의도: 이 메서드는 데이터를 변경하지 않음을 표시
     *
     * [조회 메서드에는 readOnly = true를 권장]
     * - DB에 따라 읽기 전용 연결을 사용할 수 있음
     * - 실수로 데이터 변경하는 것을 방지
     *
     * @param id 결제 ID
     * @return 결제 엔티티 (없으면 예외)
     * @throws IllegalArgumentException 결제를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Payment getPayment(Long id) {
        // orElseThrow: 값이 없으면 예외 발생
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
     * [트랜잭션 내 엔티티 수정]
     * - findById로 조회한 엔티티는 영속 상태(Persistent)
     * - 영속 상태 엔티티를 수정하면 트랜잭션 종료 시 자동 UPDATE
     * - 이를 "변경 감지(Dirty Checking)"라고 합니다
     *
     * [Dirty Checking 동작 원리]
     * 1. findById로 엔티티 조회 (스냅샷 저장)
     * 2. payment.refund()로 상태 변경
     * 3. 트랜잭션 커밋 시점에 스냅샷과 현재 상태 비교
     * 4. 변경된 부분만 UPDATE SQL 생성 및 실행
     *
     * @param id 환불할 결제 ID
     * @return 환불 처리된 결제
     */
    @Transactional
    public Payment refundPayment(Long id) {
        Payment payment = getPayment(id);  // 영속 상태로 조회
        payment.refund();                   // 상태 변경 (Dirty Checking 대상)
        log.info("결제 환불 완료: id={}", id);
        // save() 호출 불필요 - 변경 감지로 자동 UPDATE
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
