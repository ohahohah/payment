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
 * PaymentRepository - 데이터 저장소 (데이터 접근 계층)
 * ====================================================================
 *
 * [Repository 계층이란?]
 * - 데이터베이스와 직접 통신하는 계층입니다
 * - CRUD(생성, 조회, 수정, 삭제) 작업을 담당합니다
 * - Service 계층에서 호출됩니다
 *
 * [Spring Data JPA란?]
 * - JPA(Java Persistence API)를 쉽게 사용할 수 있게 도와주는 프레임워크
 * - 인터페이스만 정의하면 구현체를 자동 생성합니다
 * - SQL 없이 메서드 이름만으로 쿼리 생성 가능
 *
 * [JpaRepository<Payment, Long>]
 * - Payment: 다루는 엔티티 타입
 * - Long: 엔티티의 ID(기본키) 타입
 *
 * [기본 제공 메서드] (JpaRepository가 제공)
 * - save(entity): 저장 또는 수정
 * - findById(id): ID로 조회
 * - findAll(): 전체 조회
 * - delete(entity): 삭제
 * - count(): 개수 조회
 *
 * [@Repository]
 * - 이 인터페이스를 스프링 빈으로 등록합니다
 * - 데이터 접근 예외를 스프링 예외로 변환합니다
 * - 생략 가능 (JpaRepository 상속 시 자동 인식)
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * [상태별 결제 조회] - 특정 상태의 결제 목록을 조회합니다
     *
     * [쿼리 메서드 (Query Method)]
     * - 메서드 이름으로 쿼리를 자동 생성합니다
     * - findBy + 필드명 형식으로 작성
     *
     * [자동 생성되는 SQL]
     * SELECT * FROM payments WHERE stat = ?
     *
     * [명명 규칙]
     * - findBy: SELECT WHERE
     * - And: AND 조건
     * - Or: OR 조건
     * - OrderBy: 정렬
     * - GreaterThan, LessThan: 비교 연산
     *
     * @param stat 조회할 상태 (P, C, F, R)
     * @return 해당 상태의 결제 목록
     */
    List<Payment> findByStat(PaymentStatus stat);

    /**
     * [국가별 결제 조회] - 특정 국가의 결제 목록을 조회합니다
     *
     * [자동 생성되는 SQL]
     * SELECT * FROM payments WHERE cd = ?
     *
     * @param cd 국가 코드 (KR, US 등)
     * @return 해당 국가의 결제 목록
     */
    List<Payment> findByCd(String cd);

    /**
     * [상태 + 국가 조건 조회] - 상태와 국가 조건을 모두 만족하는 결제를 조회합니다
     *
     * [And 키워드]
     * - 두 조건을 AND로 연결합니다
     *
     * [자동 생성되는 SQL]
     * SELECT * FROM payments WHERE stat = ? AND cd = ?
     *
     * @param stat 상태
     * @param cd 국가 코드
     * @return 조건을 만족하는 결제 목록
     */
    List<Payment> findByStatAndCd(PaymentStatus stat, String cd);

    /**
     * [특정 금액 이상 조회] - 세금 적용 후 금액이 특정 값보다 큰 결제를 조회합니다
     *
     * [GreaterThan]
     * - 필드값 > 파라미터값 조건
     * - GreaterThanEqual, LessThan, LessThanEqual도 있음
     *
     * [자동 생성되는 SQL]
     * SELECT * FROM payments WHERE amt3 > ?
     *
     * @param amt 기준 금액
     * @return 기준 금액보다 큰 결제 목록
     */
    List<Payment> findByAmt3GreaterThan(Double amt);

    /**
     * [기간별 조회] - 특정 기간에 생성된 결제를 조회합니다
     *
     * [Between]
     * - start <= 필드값 <= end 조건
     *
     * [자동 생성되는 SQL]
     * SELECT * FROM payments WHERE cdt BETWEEN ? AND ?
     *
     * @param start 시작 일시
     * @param end 종료 일시
     * @return 해당 기간의 결제 목록
     */
    List<Payment> findByCdtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * [복합 조건 + 정렬] - VIP 여부와 상태로 조회 후 생성일시 내림차순 정렬
     *
     * [OrderBy...Desc]
     * - 정렬 조건을 메서드명에 포함
     * - Asc: 오름차순, Desc: 내림차순
     *
     * [자동 생성되는 SQL]
     * SELECT * FROM payments
     * WHERE flag = ? AND stat = ?
     * ORDER BY cdt DESC
     *
     * @param flag VIP 여부
     * @param stat 상태
     * @return 조건을 만족하는 결제 목록 (최신순)
     */
    List<Payment> findByFlagAndStatOrderByCdtDesc(Boolean flag, PaymentStatus stat);

    /**
     * [국가별 결제 건수] - 특정 국가의 결제 건수를 조회합니다
     *
     * [@Query 어노테이션]
     * - 직접 JPQL(Java Persistence Query Language)을 작성합니다
     * - 복잡한 쿼리나 쿼리 메서드로 표현하기 어려운 경우 사용
     *
     * [JPQL vs SQL]
     * - JPQL: 엔티티와 필드명 사용 (Payment, p.cd)
     * - SQL: 테이블과 컬럼명 사용 (payments, cd)
     *
     * [:cd 파라미터]
     * - @Param("cd")로 바인딩
     * - 이름 기반 파라미터
     *
     * @param cd 국가 코드
     * @return 결제 건수
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.cd = :cd")
    long countByCd(@Param("cd") String cd);

    /**
     * [상태별 총액 조회] - 특정 상태 결제의 세금 적용 후 금액 합계를 조회합니다
     *
     * [COALESCE 함수]
     * - NULL이면 두 번째 인자(0)를 반환합니다
     * - 결제가 없어도 NULL 대신 0을 반환
     *
     * [SUM 집계 함수]
     * - 해당 컬럼의 합계를 계산합니다
     *
     * @param stat 상태 (주로 C - 완료)
     * @return 총액 (없으면 0)
     */
    @Query("SELECT COALESCE(SUM(p.amt3), 0) FROM Payment p WHERE p.stat = :stat")
    Double sumAmt3ByStat(@Param("stat") PaymentStatus stat);

    /**
     * [최근 결제 N건 조회] - 생성일시 기준 최근 N건을 조회합니다
     *
     * [네이티브 쿼리 (Native Query)]
     * - nativeQuery = true로 설정하면 SQL을 직접 사용
     * - DB 종속적이지만 성능 최적화에 유용
     * - JPQL로 표현하기 어려운 쿼리에 사용
     *
     * [LIMIT 절]
     * - 조회 결과 개수 제한
     * - MySQL, PostgreSQL, H2 등에서 지원
     * - Oracle은 ROWNUM, SQL Server는 TOP 사용
     *
     * [주의사항]
     * - 네이티브 쿼리는 DB 변경 시 수정 필요할 수 있음
     * - 테이블명, 컬럼명은 실제 DB 구조와 일치해야 함
     *
     * @param limit 조회할 건수
     * @return 최근 결제 목록
     */
    @Query(value = "SELECT * FROM payments ORDER BY cdt DESC LIMIT :limit",
           nativeQuery = true)
    List<Payment> findRecent(@Param("limit") int limit);
}
