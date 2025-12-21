package com.example.payment_ddd.application.service;

import com.example.payment_ddd.application.command.CreatePaymentCommand;
import com.example.payment_ddd.application.command.RefundPaymentCommand;
import com.example.payment_ddd.application.eventhandler.DomainEventHandler;
import com.example.payment_ddd.domain.event.DomainEvent;
import com.example.payment_ddd.domain.model.Country;
import com.example.payment_ddd.domain.model.Money;
import com.example.payment_ddd.domain.model.Payment;
import com.example.payment_ddd.domain.repository.PaymentRepository;
import com.example.payment_ddd.domain.service.PaymentDomainService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PaymentCommandService - 결제 애플리케이션 서비스
 *
 * [Application Service의 역할]
 * 1. 유스케이스 조율 (Orchestration)
 * 2. 트랜잭션 관리
 * 3. 도메인 이벤트 발행
 * 4. 인프라 서비스 호출 (Repository 등)
 *
 * [Application Service vs Domain Service]
 * - Application Service: 도메인 로직을 포함하지 않음, 조율만 담당
 * - Domain Service: 순수 비즈니스 로직, 인프라에 의존하지 않음
 *
 * [계층 분리의 이점]
 * - 테스트 용이성: 도메인 로직은 인프라 없이 테스트 가능
 * - 유지보수성: 각 계층의 책임이 명확
 * - 유연성: 인프라 변경 시 도메인 영향 없음
 */
public class PaymentCommandService {

    private final PaymentDomainService paymentDomainService;
    private final PaymentRepository paymentRepository;
    private final List<DomainEventHandler<?>> eventHandlers;

    public PaymentCommandService(PaymentDomainService paymentDomainService,
                                  PaymentRepository paymentRepository,
                                  List<DomainEventHandler<?>> eventHandlers) {
        this.paymentDomainService = paymentDomainService;
        this.paymentRepository = paymentRepository;
        this.eventHandlers = eventHandlers;
    }

    /**
     * 결제 생성 및 완료
     *
     * [유스케이스 조율]
     * 1. Domain Service를 통해 Payment 생성 (도메인 로직)
     * 2. Payment 상태 변경 (엔티티의 비즈니스 메서드)
     * 3. Repository를 통해 저장 (인프라)
     * 4. 도메인 이벤트 발행 (Application 책임)
     */
    @Transactional
    public Payment createAndCompletePayment(CreatePaymentCommand command) {
        // 1. 도메인 서비스를 통해 Payment 생성
        Money originalPrice = Money.of(command.amount());
        Country country = Country.of(command.country());
        Payment payment = paymentDomainService.createPayment(originalPrice, country, command.isVip());

        // 2. 결제 완료 처리 (엔티티의 비즈니스 메서드)
        payment.complete();

        // 3. 저장
        Payment savedPayment = paymentRepository.save(payment);

        // 4. 도메인 이벤트 발행
        publishEvents(savedPayment);

        return savedPayment;
    }

    /**
     * 결제 환불
     */
    @Transactional
    public Payment refundPayment(RefundPaymentCommand command) {
        // 1. 결제 조회
        Payment payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다: " + command.paymentId()));

        // 2. 환불 처리 (엔티티의 비즈니스 메서드)
        payment.refund();

        // 3. 저장
        Payment savedPayment = paymentRepository.save(payment);

        // 4. 도메인 이벤트 발행
        publishEvents(savedPayment);

        return savedPayment;
    }

    /**
     * 결제 조회
     */
    @Transactional(readOnly = true)
    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다: " + paymentId));
    }

    /**
     * 모든 결제 조회
     */
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * 도메인 이벤트 발행
     *
     * [이벤트 발행이 Application Service에 있는 이유]
     * - 트랜잭션 완료 후 이벤트 발행 보장
     * - 도메인은 이벤트를 "등록"만 하고, 발행은 Application이 담당
     */
    @SuppressWarnings("unchecked")
    private void publishEvents(Payment payment) {
        List<DomainEvent> events = payment.pullDomainEvents();

        for (DomainEvent event : events) {
            for (DomainEventHandler<?> handler : eventHandlers) {
                if (handler.supportedEventType().isInstance(event)) {
                    ((DomainEventHandler<DomainEvent>) handler).handle(event);
                }
            }
        }
    }
}
