package com.example.payment.service;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResult;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.observer.PaymentObserver;
import com.example.payment.strategy.discount.DiscountStrategy;
import com.example.payment.strategy.tax.TaxStrategy;
import com.example.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ====================================================================
 * PaymentService - 결제 처리 서비스 (비즈니스 로직 담당)
 * ====================================================================
 *
 * [Service 계층이란?]
 * - 비즈니스 로직을 처리하는 계층입니다
 * - Controller(요청/응답)와 Repository(데이터 저장) 사이에 위치합니다
 * - 하나의 비즈니스 기능을 완성하는 역할을 합니다
 *
 * [계층 구조 (Layered Architecture)]
 * Controller → Service → Repository → Database
 *    ↓           ↓           ↓
 * 요청처리   비즈니스로직   데이터저장
 *
 * [@Service 어노테이션]
 * - 이 클래스를 스프링 빈(Bean)으로 등록합니다
 * - 스프링 컨테이너가 객체 생성과 생명주기를 관리합니다
 * - @Component와 동일하지만, Service 계층임을 명시적으로 표현합니다
 *
 * [의존성 주입 (Dependency Injection, DI)]
 * - 생성자를 통해 필요한 객체들을 외부에서 주입받습니다
 * - 스프링이 자동으로 필요한 빈들을 찾아서 주입합니다
 * - 장점: 테스트 시 Mock 객체로 쉽게 교체 가능
 */
@Service
public class PaymentService {

    /**
     * [Logger 사용]
     * - SLF4J(Simple Logging Facade for Java)를 사용합니다
     * - 로그 레벨: TRACE < DEBUG < INFO < WARN < ERROR
     * - 개발 시에는 DEBUG, 운영 시에는 INFO 이상을 사용합니다
     */
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final DiscountStrategy discountStrategy;
    private final TaxStrategy taxStrategy;
    private final List<PaymentObserver> observers;

    /**
     * [생성자 주입 (Constructor Injection)]
     * - 스프링 4.3부터 생성자가 하나면 @Autowired 생략 가능
     * - 필드 주입(@Autowired)보다 생성자 주입이 권장됨
     * - 이유: 불변성 보장, 테스트 용이성, 순환 참조 방지
     *
     * [List<PaymentListener> 주입]
     * - 스프링이 PaymentListener를 구현한 모든 빈을 List로 수집
     * - @Component가 붙은 LoggingListener, SettlementListener 등이 자동 수집됨
     * - 새 리스너 추가 시 코드 수정 없이 빈 등록만 하면 됨 (OCP 원칙)
     */
    public PaymentService(PaymentRepository paymentRepository,
                          DiscountStrategy discountStrategy,
                          TaxStrategy taxStrategy,
                          List<PaymentObserver> observers) {
        this.paymentRepository = paymentRepository;
        this.discountStrategy = discountStrategy;
        this.taxStrategy = taxStrategy;
        this.observers = observers;
    }

    /**
     * [결제 처리] - 결제 요청을 받아 할인/세금 적용 후 저장합니다
     *
     * [처리 흐름]
     * 1. 요청 유효성 검증 (금액이 0 이상인지)
     * 2. 할인 정책 적용 (VIP는 15%, 일반은 10%)
     * 3. 세금 정책 적용 (한국 10%, 미국 7%)
     * 4. 결제 엔티티 생성 및 DB 저장
     * 5. 결제 상태를 완료(C)로 변경
     * 6. 등록된 리스너들에게 완료 알림 (로깅, 정산 등)
     *
     * [@Transactional]
     * - 이 메서드를 하나의 트랜잭션으로 처리합니다
     * - 메서드 내에서 예외 발생 시 모든 DB 변경이 롤백됩니다
     * - 성공 시 자동으로 커밋됩니다
     *
     * [트랜잭션이란?]
     * - 여러 DB 작업을 하나의 단위로 묶는 것
     * - 모두 성공하거나, 모두 실패하거나 (원자성)
     * - 예: 결제 저장은 성공했는데 상태 변경은 실패하면 → 전체 롤백
     *
     * @param request 결제 요청 정보 (금액, 국가, VIP여부)
     * @return 결제 결과 (원래 금액, 할인 후, 세금 후)
     * @throws IllegalArgumentException 금액이 음수인 경우
     */
    @Transactional
    public PaymentResult execute(PaymentRequest request) {
        log.debug("처리 시작: amt1={}, cd={}, flag={}",
                request.amt1(), request.cd(), request.flag());

        // 1. 유효성 검증
        if (request.amt1() < 0) {
            throw new IllegalArgumentException("잘못된 값");
        }

        // 2. 할인 적용 (VIP 15%, 일반 10%)
        double v1 = discountStrategy.apply(request.amt1(), request.flag());

        // 3. 세금 적용 (한국 10%, 미국 7%)
        double v2 = taxStrategy.apply(v1);

        // 4. 결과 DTO 생성
        PaymentResult result = new PaymentResult(
                request.amt1(), v1, v2,
                request.cd(), request.flag()
        );

        // 5. 엔티티 생성 및 저장
        Payment payment = Payment.create(
                result.amt1(),
                result.amt2(),
                result.amt3(),
                result.cd(),
                result.flag()
        );

        Payment saved = paymentRepository.save(payment);
        log.debug("저장 완료: id={}", saved.getId());

        // 6. 상태를 완료(C)로 변경
        saved.setStat(PaymentStatus.C);
        saved.setUdt(LocalDateTime.now());
        log.info("처리 완료: id={}, amt={}", saved.getId(), result.amt3());

        // 7. 옵저버들에게 완료 알림
        for (PaymentObserver observer : observers) {
            observer.onPaymentCompleted(result);
        }

        return result;
    }

    /**
     * [결제 단건 조회] - ID로 결제 정보를 조회합니다
     *
     * [@Transactional(readOnly = true)]
     * - 읽기 전용 트랜잭션을 설정합니다
     * - 성능 최적화: 더티 체킹(변경 감지) 비활성화
     * - DB 복제 환경에서 읽기 전용 DB(Slave)로 요청 가능
     *
     * [orElseThrow()]
     * - Optional이 비어있으면 예외를 던집니다
     * - Java 8의 Optional 패턴
     *
     * @param id 조회할 결제 ID
     * @return 결제 엔티티
     * @throws IllegalArgumentException 결제를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Payment getData(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("데이터 없음: " + id));
    }

    /**
     * [전체 결제 조회] - 모든 결제 목록을 조회합니다
     *
     * [주의사항]
     * - 데이터가 많으면 성능 문제 발생 가능
     * - 실무에서는 페이징(Pagination) 처리 필요
     * - 예: findAll(Pageable pageable) 사용
     *
     * @return 전체 결제 목록
     */
    @Transactional(readOnly = true)
    public List<Payment> getList() {
        return paymentRepository.findAll();
    }

    /**
     * [상태별 결제 조회] - 특정 상태의 결제만 조회합니다
     *
     * [PaymentStatus]
     * - P: 대기중 (Pending)
     * - C: 완료 (Completed)
     * - F: 실패 (Failed)
     * - R: 환불 (Refunded)
     *
     * @param stat 조회할 상태 (P, C, F, R)
     * @return 해당 상태의 결제 목록
     */
    @Transactional(readOnly = true)
    public List<Payment> getListByStat(PaymentStatus stat) {
        return paymentRepository.findByStat(stat);
    }

    /**
     * [결제 환불 처리] - 완료된 결제를 환불 처리합니다
     *
     * [비즈니스 규칙]
     * - 완료(C) 상태의 결제만 환불 가능
     * - 환불 후 상태가 R(Refunded)로 변경됨
     * - 수정 시간(udt)이 현재 시간으로 갱신됨
     *
     * @param id 환불할 결제 ID
     * @return 환불 처리된 결제 엔티티
     * @throws IllegalArgumentException 결제를 찾을 수 없는 경우
     * @throws IllegalStateException 환불 불가능한 상태인 경우
     */
    @Transactional
    public Payment updateStatus(Long id) {
        Payment payment = getData(id);

        // 완료(C) 상태만 환불 가능
        if (payment.getStat() != PaymentStatus.C) {
            throw new IllegalStateException("처리 불가 상태");
        }

        // 상태를 환불(R)로 변경
        payment.setStat(PaymentStatus.R);
        payment.setUdt(LocalDateTime.now());

        log.info("상태 변경 완료: id={}", id);
        return payment;
    }

    /**
     * [완료된 결제 총액 조회] - 완료 상태 결제의 총액을 조회합니다
     *
     * [JPQL 사용]
     * - Repository의 @Query로 정의된 JPQL 사용
     * - COALESCE: 결과가 NULL이면 0을 반환
     *
     * @param cd 국가 코드 (현재 사용되지 않음 - 버그)
     * @return 완료된 결제의 총액
     */
    @Transactional(readOnly = true)
    public Double getSum(String cd) {
        return paymentRepository.sumAmt3ByStat(PaymentStatus.C);
    }

    /**
     * [최근 결제 조회] - 최근 N건의 결제를 조회합니다
     *
     * [네이티브 쿼리 사용]
     * - Repository에서 nativeQuery = true로 SQL 직접 사용
     * - ORDER BY cdt DESC LIMIT :limit
     *
     * @param limit 조회할 건수
     * @return 최근 결제 목록 (생성일시 내림차순)
     */
    @Transactional(readOnly = true)
    public List<Payment> getRecent(int limit) {
        return paymentRepository.findRecent(limit);
    }
}
