package com.example.payment.repository;

import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ====================================================================
 * PaymentRepository - 결제 저장소 인터페이스 (Spring Data JPA Repository)
 * ====================================================================
 *
 * [Spring Data JPA Repository란?]
 * - 데이터 접근 계층(DAO)을 쉽게 구현할 수 있게 해주는 인터페이스입니다
 * - 기본 CRUD 메서드가 자동으로 제공됩니다
 * - SQL을 직접 작성하지 않아도 됩니다
 *
 * [JpaRepository<Entity, ID>]
 * - 제네릭 타입:
 *   - 첫 번째: 엔티티 클래스 (Payment)
 *   - 두 번째: 기본 키 타입 (Long)
 *
 * [인터페이스만 정의하면 되는 이유]
 * - Spring Data JPA가 런타임에 구현체를 자동 생성합니다 (프록시)
 * - 개발자는 인터페이스만 정의하면 됩니다
 *
 * [@Repository 어노테이션]
 * - 이 인터페이스가 저장소 역할임을 나타냅니다
 * - 실제로는 JpaRepository 상속 시 자동 등록되어 생략 가능
 * - 명시적으로 표시하면 역할이 명확해집니다
 *
 * [기본 제공 메서드 (JpaRepository)]
 * - save(entity): 저장 (INSERT/UPDATE)
 * - findById(id): ID로 조회 (SELECT)
 * - findAll(): 전체 조회 (SELECT)
 * - delete(entity): 삭제 (DELETE)
 * - count(): 개수 조회 (COUNT)
 * - existsById(id): 존재 여부 확인
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 상태별 결제 목록 조회
     *
     * [쿼리 메서드 (Query Method)]
     * - 메서드 이름으로 쿼리를 자동 생성합니다
     * - findBy + 필드명 형식
     *
     * 메서드명 → 생성되는 SQL:
     * findByStatus(status)
     * → SELECT * FROM payments WHERE status = ?
     *
     * [네이밍 규칙]
     * - findBy: SELECT ... WHERE
     * - countBy: SELECT COUNT(*) WHERE
     * - deleteBy: DELETE ... WHERE
     * - existsBy: SELECT EXISTS(...)
     *
     * @param status 조회할 결제 상태
     * @return 해당 상태의 결제 목록
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * 국가별 결제 목록 조회
     *
     * @param country 국가 코드
     * @return 해당 국가의 결제 목록
     */
    List<Payment> findByCountry(String country);

    /**
     * 상태와 국가로 조회 (AND 조건)
     *
     * [복합 조건 쿼리 메서드]
     * - And, Or 키워드로 조건 결합 가능
     *
     * 메서드명 → 생성되는 SQL:
     * findByStatusAndCountry(status, country)
     * → SELECT * FROM payments WHERE status = ? AND country = ?
     *
     * @param status 결제 상태
     * @param country 국가 코드
     * @return 조건에 맞는 결제 목록
     */
    List<Payment> findByStatusAndCountry(PaymentStatus status, String country);

    /**
     * 특정 금액 이상의 결제 조회
     *
     * [비교 연산자 키워드]
     * - GreaterThan: >
     * - GreaterThanEqual: >=
     * - LessThan: <
     * - LessThanEqual: <=
     * - Between: BETWEEN ... AND ...
     *
     * @param amount 기준 금액
     * @return 해당 금액 이상의 결제 목록
     */
    List<Payment> findByTaxedAmountGreaterThan(Double amount);

    /**
     * 특정 기간 내 결제 조회
     *
     * [Between 키워드]
     * - 범위 검색에 사용
     *
     * 메서드명 → 생성되는 SQL:
     * → SELECT * FROM payments WHERE created_at BETWEEN ? AND ?
     *
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 해당 기간의 결제 목록
     */
    List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * VIP 고객의 결제 중 특정 상태인 것 조회
     *
     * [정렬 키워드]
     * - OrderBy + 필드명 + Asc/Desc
     *
     * @param isVip VIP 여부
     * @param status 결제 상태
     * @return 조건에 맞는 결제 목록 (생성일 내림차순)
     */
    List<Payment> findByIsVipAndStatusOrderByCreatedAtDesc(Boolean isVip, PaymentStatus status);

    /**
     * 국가별 결제 건수 조회 (JPQL 사용)
     *
     * [@Query 어노테이션]
     * - 직접 JPQL 또는 네이티브 SQL을 작성합니다
     * - 복잡한 쿼리는 쿼리 메서드보다 @Query가 적합합니다
     *
     * [JPQL (Java Persistence Query Language)]
     * - 엔티티 객체를 대상으로 하는 쿼리 언어입니다
     * - SQL과 비슷하지만 테이블이 아닌 엔티티를 대상으로 합니다
     * - p.country (테이블 컬럼명이 아닌 엔티티 필드명 사용)
     *
     * [@Param 어노테이션]
     * - JPQL의 :파라미터명과 메서드 파라미터를 매핑합니다
     * - :country → @Param("country") String country
     *
     * @param country 국가 코드
     * @return 해당 국가의 결제 건수
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.country = :country")
    long countByCountry(@Param("country") String country);

    /**
     * 특정 상태의 결제 총액 조회 (JPQL 집계 함수)
     *
     * [JPQL 집계 함수]
     * - COUNT, SUM, AVG, MAX, MIN 등 SQL과 동일
     * - COALESCE: NULL일 때 기본값 반환
     *
     * @param status 결제 상태
     * @return 해당 상태의 결제 총액 (없으면 0)
     */
    @Query("SELECT COALESCE(SUM(p.taxedAmount), 0) FROM Payment p WHERE p.status = :status")
    Double sumTaxedAmountByStatus(@Param("status") PaymentStatus status);

    /**
     * 최근 결제 N건 조회 (네이티브 쿼리)
     *
     * [nativeQuery = true]
     * - JPQL 대신 실제 SQL을 사용합니다
     * - DB 벤더 종속적이지만 복잡한 쿼리에 유용합니다
     * - 테이블명, 컬럼명을 그대로 사용합니다
     *
     * [LIMIT]
     * - H2, MySQL, PostgreSQL에서 사용 가능
     * - Oracle은 ROWNUM 사용
     *
     * @param limit 조회할 건수
     * @return 최근 결제 목록
     */
    @Query(value = "SELECT * FROM payments ORDER BY created_at DESC LIMIT :limit",
           nativeQuery = true)
    List<Payment> findRecentPayments(@Param("limit") int limit);
}
