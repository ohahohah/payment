package com.example.payment_ddd_v1.domain.repository;

import com.example.payment_ddd_v1.domain.model.Payment;

import java.util.List;
import java.util.Optional;

/**
 * PaymentRepository - 결제 저장소 인터페이스
 *
 * [Repository 패턴]
 * - Domain 계층에 인터페이스 정의
 * - Infrastructure 계층에서 구현
 * - 의존성 역전 원칙(DIP) 적용
 *
 * [왜 인터페이스를 Domain에 두나요?]
 * - Domain이 Infrastructure에 의존하지 않음
 * - 테스트 시 Mock으로 쉽게 교체 가능
 * - DB 기술 변경 시 Domain 코드 수정 불필요
 */
public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    List<Payment> findAll();
}
