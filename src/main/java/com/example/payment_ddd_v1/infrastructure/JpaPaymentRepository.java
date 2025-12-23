package com.example.payment_ddd_v1.infrastructure;

import com.example.payment_ddd_v1.domain.model.Payment;
import com.example.payment_ddd_v1.domain.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JpaPaymentRepository - PaymentRepository 구현체
 *
 * ============================================================================
 * [Infrastructure 계층]
 * ============================================================================
 *
 * - Domain 인터페이스(PaymentRepository)의 구현체
 * - 실제 데이터 저장 기술 담당 (JPA)
 * - Spring Data JPA를 내부적으로 사용
 *
 * ============================================================================
 * [의존성 역전 원칙 (DIP)]
 * ============================================================================
 *
 * Domain 계층:
 *   PaymentRepository (인터페이스) <- PaymentService가 의존
 *
 * Infrastructure 계층:
 *   JpaPaymentRepository (구현체) -> PaymentRepository 구현
 *
 * → Domain은 Infrastructure를 모름!
 * → 기술 변경 시 구현체만 교체
 *
 * ============================================================================
 * [왜 Spring Data JPA를 직접 상속하지 않나요?]
 * ============================================================================
 *
 * 직접 상속 시:
 *   public interface PaymentRepository extends JpaRepository<Payment, Long>
 *   → Domain 계층이 Spring Data에 의존하게 됨
 *
 * 현재 구조:
 *   Domain: PaymentRepository (순수 인터페이스)
 *   Infra: JpaPaymentRepository → SpringDataPaymentRepository
 *   → Domain 계층은 Spring을 몰라도 됨
 */
@Repository
public class JpaPaymentRepository implements PaymentRepository {

    private final SpringDataPaymentRepository springDataRepository;

    public JpaPaymentRepository(SpringDataPaymentRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Payment save(Payment payment) {
        return springDataRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return springDataRepository.findById(id);
    }

    @Override
    public List<Payment> findAll() {
        return springDataRepository.findAll();
    }
}
