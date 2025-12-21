package com.example.payment_ddd.application.service;

import com.example.payment_ddd.application.command.CreatePaymentCommand;
import com.example.payment_ddd.application.command.RefundPaymentCommand;
import com.example.payment_ddd.application.eventhandler.DomainEventHandler;
import com.example.payment_ddd.application.eventhandler.LoggingEventHandler;
import com.example.payment_ddd.domain.model.*;
import com.example.payment_ddd.domain.policy.*;
import com.example.payment_ddd.domain.repository.PaymentRepository;
import com.example.payment_ddd.domain.service.PaymentDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * PaymentCommandServiceTest - 애플리케이션 서비스 단위 테스트
 *
 * [Application Service 테스트 특징]
 * - Mock Repository 사용 (인메모리 구현)
 * - 순수 Java 테스트
 * - 유스케이스 흐름 검증
 */
@DisplayName("PaymentCommandService 테스트")
class PaymentCommandServiceTest {

    private PaymentCommandService paymentCommandService;
    private InMemoryPaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        // 도메인 서비스 설정
        DiscountPolicy discountPolicy = new VipDiscountPolicy();
        List<TaxPolicy> taxPolicies = List.of(new KoreaTaxPolicy(), new UsTaxPolicy());
        PaymentDomainService paymentDomainService = new PaymentDomainService(discountPolicy, taxPolicies);

        // 인메모리 저장소
        paymentRepository = new InMemoryPaymentRepository();

        // 이벤트 핸들러
        List<DomainEventHandler<?>> eventHandlers = List.of(new LoggingEventHandler());

        // 애플리케이션 서비스
        paymentCommandService = new PaymentCommandService(
                paymentDomainService,
                paymentRepository,
                eventHandlers
        );
    }

    @Nested
    @DisplayName("결제 생성 및 완료")
    class CreateAndCompletePaymentTest {

        @Test
        @DisplayName("결제 생성 및 완료 성공")
        void createAndCompletePayment() {
            CreatePaymentCommand command = new CreatePaymentCommand(10000, "KR", true);

            Payment payment = paymentCommandService.createAndCompletePayment(command);

            assertThat(payment.getId()).isNotNull();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(payment.getTaxedAmount().getAmount()).isEqualTo(9900);
        }

        @Test
        @DisplayName("저장소에 저장됨")
        void savedToRepository() {
            CreatePaymentCommand command = new CreatePaymentCommand(10000, "KR", true);

            Payment payment = paymentCommandService.createAndCompletePayment(command);

            Optional<Payment> found = paymentRepository.findById(payment.getId());
            assertThat(found).isPresent();
        }
    }

    @Nested
    @DisplayName("환불")
    class RefundPaymentTest {

        @Test
        @DisplayName("환불 성공")
        void refundPayment() {
            // Given: 완료된 결제
            CreatePaymentCommand createCommand = new CreatePaymentCommand(10000, "KR", true);
            Payment payment = paymentCommandService.createAndCompletePayment(createCommand);

            // When: 환불
            RefundPaymentCommand refundCommand = new RefundPaymentCommand(payment.getId());
            Payment refundedPayment = paymentCommandService.refundPayment(refundCommand);

            // Then
            assertThat(refundedPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("존재하지 않는 결제 환불 시 예외")
        void refundNonExistentPayment() {
            RefundPaymentCommand command = new RefundPaymentCommand(999L);

            assertThatThrownBy(() -> paymentCommandService.refundPayment(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("조회")
    class QueryTest {

        @Test
        @DisplayName("ID로 조회")
        void getPaymentById() {
            CreatePaymentCommand command = new CreatePaymentCommand(10000, "KR", false);
            Payment created = paymentCommandService.createAndCompletePayment(command);

            Payment found = paymentCommandService.getPayment(created.getId());

            assertThat(found.getId()).isEqualTo(created.getId());
        }

        @Test
        @DisplayName("모든 결제 조회")
        void getAllPayments() {
            paymentCommandService.createAndCompletePayment(new CreatePaymentCommand(10000, "KR", true));
            paymentCommandService.createAndCompletePayment(new CreatePaymentCommand(20000, "US", false));

            List<Payment> payments = paymentCommandService.getAllPayments();

            assertThat(payments).hasSize(2);
        }
    }

    /**
     * 테스트용 인메모리 Repository 구현
     */
    private static class InMemoryPaymentRepository implements PaymentRepository {
        private final Map<Long, Payment> store = new HashMap<>();
        private long idSequence = 1L;

        @Override
        public Payment save(Payment payment) {
            if (payment.getId() == null) {
                payment.assignId(idSequence++);
            }
            // 복사본 저장 (도메인 객체 상태 변경 추적 방지)
            store.put(payment.getId(), payment);
            return payment;
        }

        @Override
        public Optional<Payment> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Payment> findByStatus(PaymentStatus status) {
            return store.values().stream()
                    .filter(p -> p.getStatus() == status)
                    .toList();
        }

        @Override
        public List<Payment> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public void deleteById(Long id) {
            store.remove(id);
        }
    }
}
