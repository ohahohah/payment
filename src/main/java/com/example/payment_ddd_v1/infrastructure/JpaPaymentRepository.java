package com.example.payment_ddd_v1.infrastructure;

import com.example.payment_ddd_v1.domain.model.Money;
import com.example.payment_ddd_v1.domain.model.Payment;
import com.example.payment_ddd_v1.domain.model.PaymentStatus;
import com.example.payment_ddd_v1.domain.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JpaPaymentRepository - PaymentRepository 구현체
 *
 * [Infrastructure 계층]
 * - Domain 인터페이스의 구현체
 * - 실제 데이터 저장 기술 담당 (JPA, JDBC, Memory 등)
 *
 * [현재 구현]
 * - 단순화를 위해 In-Memory 저장소 사용
 * - 실제로는 JPA EntityManager 또는 Spring Data JPA 사용
 */
@Repository
public class JpaPaymentRepository implements PaymentRepository {

    private final Map<Long, Payment> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            payment.setId(sequence.getAndIncrement());
        }
        store.put(payment.getId(), payment);
        return payment;
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Payment> findAll() {
        return List.copyOf(store.values());
    }
}
