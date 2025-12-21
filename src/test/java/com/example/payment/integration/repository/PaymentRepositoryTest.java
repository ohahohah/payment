package com.example.payment.integration.repository;

import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ============================================================================
 * [GOOD] PaymentRepositoryTest - Repository 슬라이스 테스트
 * ============================================================================
 *
 * [@DataJpaTest]
 * - JPA 관련 컴포넌트만 로딩하는 슬라이스 테스트
 * - @SpringBootTest보다 훨씬 가벼움 (필요한 빈만 로딩)
 * - 내장 H2 데이터베이스 자동 설정
 * - 각 테스트 후 자동 롤백 (데이터 격리)
 *
 * [슬라이스 테스트 (Slice Test)란?]
 * - 애플리케이션의 특정 계층만 테스트
 * - 필요한 빈만 로딩하여 테스트 속도 향상
 *
 * 다른 슬라이스 테스트:
 * - @WebMvcTest: Controller 레이어
 * - @DataJpaTest: JPA Repository 레이어
 * - @JsonTest: JSON 직렬화/역직렬화
 * - @RestClientTest: REST 클라이언트
 *
 * [TestEntityManager]
 * - 테스트용 EntityManager
 * - 테스트 데이터를 직접 영속화하고 조회 가능
 * - persistAndFlush(): 저장 + 즉시 DB에 반영
 * - find(): DB에서 직접 조회
 */
@DataJpaTest
@DisplayName("PaymentRepository 슬라이스 테스트")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * [TestEntityManager]
     * - @DataJpaTest에서 자동 주입
     * - 테스트 데이터 설정에 유용
     * - Repository를 거치지 않고 직접 DB 조작 가능
     */
    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        // 각 테스트 전 데이터 초기화
        // @DataJpaTest는 자동 롤백되므로 명시적 삭제 불필요
    }

    @Nested
    @DisplayName("기본 CRUD 테스트")
    class CrudTest {

        @Test
        @DisplayName("결제를 저장하면 ID가 생성된다")
        void shouldGenerateIdWhenSaving() {
            // Given
            Payment payment = createTestPayment();

            // When
            Payment saved = paymentRepository.save(payment);

            // Then
            assertThat(saved.getId())
                    .as("저장 후 ID가 자동 생성되어야 합니다")
                    .isNotNull()
                    .isPositive();
        }

        @Test
        @DisplayName("저장된 결제를 ID로 조회할 수 있다")
        void shouldFindById() {
            // Given - TestEntityManager로 직접 저장
            Payment payment = createTestPayment();
            payment.setStatus(PaymentStatus.COMPLETED);
            Payment saved = entityManager.persistAndFlush(payment);

            // 영속성 컨텍스트 초기화 (1차 캐시 제거)
            entityManager.clear();

            // When - Repository로 조회
            Payment found = paymentRepository.findById(saved.getId())
                    .orElseThrow();

            // Then
            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getOriginalPrice()).isEqualTo(10000.0);
            assertThat(found.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("전체 결제 목록을 조회할 수 있다")
        void shouldFindAll() {
            // Given
            Payment payment1 = createTestPayment();
            Payment payment2 = createTestPayment(20000.0);
            entityManager.persistAndFlush(payment1);
            entityManager.persistAndFlush(payment2);
            entityManager.clear();

            // When
            List<Payment> payments = paymentRepository.findAll();

            // Then
            assertThat(payments)
                    .hasSize(2)
                    .extracting(Payment::getOriginalPrice)
                    .containsExactlyInAnyOrder(10000.0, 20000.0);
        }

        @Test
        @DisplayName("결제를 삭제할 수 있다")
        void shouldDelete() {
            // Given
            Payment payment = createTestPayment();
            Payment saved = entityManager.persistAndFlush(payment);
            Long id = saved.getId();
            entityManager.clear();

            // When
            paymentRepository.deleteById(id);
            entityManager.flush();

            // Then
            assertThat(paymentRepository.findById(id)).isEmpty();
        }
    }

    @Nested
    @DisplayName("쿼리 메서드 테스트")
    class QueryMethodTest {

        @Test
        @DisplayName("상태별로 결제를 조회할 수 있다")
        void shouldFindByStatus() {
            // Given
            Payment completed1 = createCompletedPayment(10000.0);
            Payment completed2 = createCompletedPayment(20000.0);
            Payment pending = createTestPayment(30000.0);

            entityManager.persistAndFlush(completed1);
            entityManager.persistAndFlush(completed2);
            entityManager.persistAndFlush(pending);
            entityManager.clear();

            // When
            List<Payment> completedPayments = paymentRepository.findByStatus(PaymentStatus.COMPLETED);
            List<Payment> pendingPayments = paymentRepository.findByStatus(PaymentStatus.PENDING);

            // Then
            assertThat(completedPayments)
                    .hasSize(2)
                    .allMatch(p -> p.getStatus() == PaymentStatus.COMPLETED);

            assertThat(pendingPayments)
                    .hasSize(1)
                    .allMatch(p -> p.getStatus() == PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("국가별로 결제를 조회할 수 있다")
        void shouldFindByCountry() {
            // Given
            Payment kr1 = createTestPayment(10000.0, "KR");
            Payment kr2 = createTestPayment(20000.0, "KR");
            Payment us = createTestPayment(30000.0, "US");

            entityManager.persistAndFlush(kr1);
            entityManager.persistAndFlush(kr2);
            entityManager.persistAndFlush(us);
            entityManager.clear();

            // When
            List<Payment> krPayments = paymentRepository.findByCountry("KR");
            List<Payment> usPayments = paymentRepository.findByCountry("US");

            // Then
            assertThat(krPayments).hasSize(2);
            assertThat(usPayments).hasSize(1);
        }

        @Test
        @DisplayName("특정 금액 이상의 결제를 조회할 수 있다")
        void shouldFindByTaxedAmountGreaterThan() {
            // Given
            Payment small = createTestPayment(5000.0);   // taxed: 5500
            Payment medium = createTestPayment(10000.0); // taxed: 9350 (VIP)
            Payment large = createTestPayment(100000.0); // taxed: 93500 (VIP)

            entityManager.persistAndFlush(small);
            entityManager.persistAndFlush(medium);
            entityManager.persistAndFlush(large);
            entityManager.clear();

            // When - 10000 이상인 결제 조회
            List<Payment> largePayments = paymentRepository.findByTaxedAmountGreaterThan(10000.0);

            // Then
            assertThat(largePayments)
                    .hasSize(1)
                    .allMatch(p -> p.getTaxedAmount() > 10000);
        }
    }

    @Nested
    @DisplayName("JPQL 쿼리 테스트")
    class JpqlQueryTest {

        @Test
        @DisplayName("국가별 결제 건수를 조회할 수 있다")
        void shouldCountByCountry() {
            // Given
            entityManager.persistAndFlush(createTestPayment(10000.0, "KR"));
            entityManager.persistAndFlush(createTestPayment(20000.0, "KR"));
            entityManager.persistAndFlush(createTestPayment(30000.0, "US"));
            entityManager.clear();

            // When
            long krCount = paymentRepository.countByCountry("KR");
            long usCount = paymentRepository.countByCountry("US");

            // Then
            assertThat(krCount).isEqualTo(2);
            assertThat(usCount).isEqualTo(1);
        }

        @Test
        @DisplayName("상태별 결제 총액을 조회할 수 있다")
        void shouldSumTaxedAmountByStatus() {
            // Given
            Payment completed1 = createCompletedPayment(10000.0);  // taxed: 9350
            Payment completed2 = createCompletedPayment(20000.0);  // taxed: 18700

            entityManager.persistAndFlush(completed1);
            entityManager.persistAndFlush(completed2);
            entityManager.clear();

            // When
            Double totalAmount = paymentRepository.sumTaxedAmountByStatus(PaymentStatus.COMPLETED);

            // Then
            assertThat(totalAmount)
                    .as("COMPLETED 상태 결제의 총액")
                    .isEqualTo(9350.0 + 18700.0);  // 28050
        }

        @Test
        @DisplayName("해당 상태의 결제가 없으면 총액은 0이다")
        void shouldReturnZeroWhenNoPayments() {
            // Given - 결제 없음

            // When
            Double totalAmount = paymentRepository.sumTaxedAmountByStatus(PaymentStatus.COMPLETED);

            // Then
            assertThat(totalAmount)
                    .as("결제가 없으면 0을 반환해야 합니다")
                    .isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("네이티브 쿼리 테스트")
    class NativeQueryTest {

        @Test
        @DisplayName("최근 결제 N건을 조회할 수 있다")
        void shouldFindRecentPayments() {
            // Given - 시간 순서대로 저장
            Payment old = createTestPayment(10000.0);
            Payment middle = createTestPayment(20000.0);
            Payment recent = createTestPayment(30000.0);

            entityManager.persistAndFlush(old);
            entityManager.persistAndFlush(middle);
            entityManager.persistAndFlush(recent);
            entityManager.clear();

            // When
            List<Payment> recentPayments = paymentRepository.findRecentPayments(2);

            // Then
            assertThat(recentPayments)
                    .hasSize(2)
                    .as("최근 2건이 반환되어야 합니다");
        }
    }

    // ========================================================================
    // 테스트 헬퍼 메서드
    // ========================================================================

    private Payment createTestPayment() {
        return createTestPayment(10000.0);
    }

    private Payment createTestPayment(double originalPrice) {
        return createTestPayment(originalPrice, "KR");
    }

    private Payment createTestPayment(double originalPrice, String country) {
        double discounted = originalPrice * 0.85;  // VIP 할인
        double taxed = discounted * 1.1;           // 한국 세금
        return Payment.create(originalPrice, discounted, taxed, country, true);
    }

    private Payment createCompletedPayment(double originalPrice) {
        Payment payment = createTestPayment(originalPrice);
        payment.setStatus(PaymentStatus.COMPLETED);
        return payment;
    }
}
