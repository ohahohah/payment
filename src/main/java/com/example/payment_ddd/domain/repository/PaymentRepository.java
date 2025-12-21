package com.example.payment_ddd.domain.repository;

import com.example.payment_ddd.domain.model.Payment;
import com.example.payment_ddd.domain.model.PaymentStatus;

import java.util.List;
import java.util.Optional;

/**
 * PaymentRepository - 결제 저장소 인터페이스
 *
 * [Repository 패턴]
 * - 도메인 객체의 컬렉션처럼 동작
 * - 영속성 메커니즘을 추상화
 * - 도메인 레이어는 인터페이스만 알고, 구현체는 인프라 레이어에 위치
 *
 * [DDD에서 Repository의 역할]
 * - Aggregate Root 단위로 영속화
 * - 도메인 모델과 데이터 모델 사이의 매핑 책임
 * - 도메인 레이어의 순수성 유지 (JPA 등 기술에 의존하지 않음)
 *
 * [인터페이스가 Domain 레이어에 있는 이유]
 * - 의존성 역전 원칙(DIP) 적용
 * - 도메인이 인프라에 의존하지 않음
 * - 테스트 시 Mock 구현 용이
 */
public interface PaymentRepository {

    /**
     * 결제 저장
     *
     * @param payment 저장할 결제
     * @return 저장된 결제 (ID 할당됨)
     */
    Payment save(Payment payment);

    /**
     * ID로 결제 조회
     *
     * @param id 결제 ID
     * @return 결제 Optional
     */
    Optional<Payment> findById(Long id);

    /**
     * 상태별 결제 목록 조회
     *
     * @param status 결제 상태
     * @return 결제 목록
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * 모든 결제 조회
     *
     * @return 모든 결제 목록
     */
    List<Payment> findAll();

    /**
     * 결제 삭제
     *
     * @param id 삭제할 결제 ID
     */
    void deleteById(Long id);
}
