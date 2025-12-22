package com.example.payment_step2.repository;

import com.example.payment_step2.domain.model.Country;
import com.example.payment_step2.domain.model.Money;
import com.example.payment_step2.entity.Payment;
import com.example.payment_step2.entity.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PaymentRepository 테스트
 *
 * [@DataJpaTest 사용]
 * - JPA 관련 빈만 로드 (빠른 테스트)
 * - H2 인메모리 DB 사용
 * - @Convert가 제대로 동작하는지 검증
 *
 * [테스트 범위]
 * - DB 저장/조회 동작 검증
 * - @Convert를 통한 Value Object 변환 검증
 */
@DataJpaTest
@ActiveProfiles("test")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("결제 저장 및 조회 - @Convert로 Money/Country 변환")
    void saveAndFind() {
        // given
        Payment payment = Payment.create(
                Money.of(10000),
                Money.of(8500),
                Money.of(9350),
                Country.korea(),
                true
        );

        // when
        Payment saved = paymentRepository.save(payment);
        Optional<Payment> found = paymentRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();

        // @Convert가 제대로 동작하여 Money/Country로 복원되었는지 확인
        assertThat(found.get().getOriginalPrice()).isEqualTo(Money.of(10000));
        assertThat(found.get().getCountry()).isEqualTo(Country.korea());
    }

    @Test
    @DisplayName("상태별 조회")
    void findByStatus() {
        // given
        Payment payment1 = createAndSavePayment(true);
        payment1.setStatus(PaymentStatus.COMPLETED);  // setter로 상태 변경 (payment_ul과 동일)
        paymentRepository.save(payment1);

        Payment payment2 = createAndSavePayment(false);
        // payment2는 PENDING 상태 유지

        // when
        var completedPayments = paymentRepository.findByStatus(PaymentStatus.COMPLETED);
        var pendingPayments = paymentRepository.findByStatus(PaymentStatus.PENDING);

        // then
        assertThat(completedPayments).hasSize(1);
        assertThat(pendingPayments).hasSize(1);
    }

    private Payment createAndSavePayment(boolean isVip) {
        Payment payment = Payment.create(
                Money.of(10000),
                Money.of(8500),
                Money.of(9350),
                Country.korea(),
                isVip
        );
        return paymentRepository.save(payment);
    }
}
