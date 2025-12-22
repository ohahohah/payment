package com.example.payment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ====================================================================
 * Payment - 결제 엔티티 (JPA Entity)
 * ====================================================================
 *
 * [JPA Entity란?]
 * - 데이터베이스 테이블과 1:1로 매핑되는 자바 객체입니다
 * - 테이블의 각 행(row)이 하나의 엔티티 객체에 해당합니다
 * - JPA가 자동으로 SQL을 생성하여 CRUD 작업을 수행합니다
 *
 * [JPA (Java Persistence API)]
 * - 자바 ORM(Object-Relational Mapping) 표준 스펙
 * - 객체와 관계형 DB 테이블을 매핑하는 기술
 * - Hibernate가 대표적인 JPA 구현체
 *
 * [@Entity]
 * - 이 클래스가 JPA 엔티티임을 선언합니다
 * - JPA가 관리하는 객체로 등록됩니다
 *
 * [@Table(name = "payments")]
 * - 매핑할 테이블 이름을 지정합니다
 * - 생략 시 클래스명이 테이블명으로 사용됩니다
 *
 * [엔티티 규칙]
 * 1. @Entity 어노테이션 필수
 * 2. @Id로 기본키 지정 필수
 * 3. 기본 생성자(파라미터 없는) 필수 (protected/public)
 * 4. final 클래스 불가
 */
@Entity
@Table(name = "payments")
public class Payment {

    /**
     * [@Id] - 기본키(Primary Key) 지정
     * - 엔티티를 식별하는 고유 값입니다
     * - 테이블의 PK 컬럼과 매핑됩니다
     *
     * [@GeneratedValue(strategy = GenerationType.IDENTITY)]
     * - 기본키 생성을 DB에 위임합니다
     * - MySQL의 AUTO_INCREMENT와 동일
     * - 다른 전략: SEQUENCE(Oracle), TABLE, AUTO
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * [@Column 어노테이션]
     * - 엔티티 필드와 테이블 컬럼을 매핑합니다
     *
     * [주요 속성]
     * - nullable: NULL 허용 여부 (기본값: true)
     * - length: 문자열 길이 (기본값: 255)
     * - unique: 유니크 제약 조건
     * - updatable: UPDATE 시 포함 여부
     * - insertable: INSERT 시 포함 여부
     *
     * [DDL 자동 생성]
     * - spring.jpa.hibernate.ddl-auto=create 설정 시
     * - 이 정보를 바탕으로 테이블을 자동 생성합니다
     */
    @Column(nullable = false)
    private Double amt1;        // 원래 가격 (Original Price)

    @Column(nullable = false)
    private Double amt2;        // 할인 적용 후 금액 (Discounted Amount)

    @Column(nullable = false)
    private Double amt3;        // 세금 적용 후 최종 금액 (Taxed Amount)

    @Column(nullable = false, length = 10)
    private String cd;          // 국가 코드 (Country Code)

    @Column(nullable = false)
    private Boolean flag;       // VIP 고객 여부 (isVip)

    /**
     * [@Enumerated]
     * - Enum 타입을 컬럼에 매핑합니다
     *
     * [EnumType 종류]
     * - ORDINAL: Enum 순서를 숫자로 저장 (0, 1, 2, ...)
     *   → 순서 변경 시 데이터 불일치 발생! 사용 비권장
     * - STRING: Enum 이름을 문자열로 저장 ("P", "C", ...)
     *   → 안전하고 가독성 좋음. 권장!
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus stat; // 결제 상태 (Status)

    /**
     * [updatable = false]
     * - UPDATE 쿼리에서 이 컬럼을 제외합니다
     * - 생성 시간처럼 한 번 저장 후 변경되지 않아야 하는 필드에 사용
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime cdt;  // 생성 일시 (Created DateTime)

    @Column(nullable = false)
    private LocalDateTime udt;  // 수정 일시 (Updated DateTime)

    /**
     * [기본 생성자 - JPA 필수]
     *
     * [왜 protected인가?]
     * - JPA는 리플렉션으로 객체를 생성하므로 기본 생성자 필요
     * - protected로 외부에서 직접 생성하는 것을 방지
     * - 정적 팩토리 메서드(create)를 통해서만 생성하도록 유도
     */
    protected Payment() {
    }

    /**
     * [private 생성자]
     * - 외부에서 직접 호출 불가
     * - 정적 팩토리 메서드에서만 사용
     *
     * [초기값 설정]
     * - stat: 생성 시 기본 상태는 P(대기중)
     * - cdt/udt: 현재 시간으로 자동 설정
     */
    private Payment(Double amt1, Double amt2, Double amt3,
                    String cd, Boolean flag) {
        this.amt1 = amt1;
        this.amt2 = amt2;
        this.amt3 = amt3;
        this.cd = cd;
        this.flag = flag;
        this.stat = PaymentStatus.P;
        this.cdt = LocalDateTime.now();
        this.udt = LocalDateTime.now();
    }

    /**
     * [정적 팩토리 메서드] - 결제 엔티티를 생성합니다
     *
     * [정적 팩토리 메서드의 장점]
     * 1. 이름을 가질 수 있음 (create, of, from 등)
     * 2. 호출할 때마다 새 객체 생성이 필수가 아님
     * 3. 반환 타입의 하위 타입 반환 가능
     * 4. 입력 매개변수에 따라 다른 클래스 반환 가능
     *
     * [vs new 생성자]
     * - new Payment(...) 보다 Payment.create(...) 가 의미 명확
     * - 생성 로직을 한 곳에서 관리 가능
     *
     * @param amt1 원래 가격
     * @param amt2 할인 적용 후 금액
     * @param amt3 세금 적용 후 최종 금액
     * @param cd 국가 코드
     * @param flag VIP 여부
     * @return 새로 생성된 Payment 엔티티
     */
    public static Payment create(Double amt1, Double amt2,
                                  Double amt3, String cd, Boolean flag) {
        return new Payment(amt1, amt2, amt3, cd, flag);
    }

    // ==========================================================================
    // Getter / Setter
    // ==========================================================================

    /**
     * [Getter 메서드]
     * - 필드 값을 읽기 위한 접근자 메서드
     * - 모든 필드에 대해 Getter 제공
     */
    public Long getId() {
        return id;
    }

    public Double getAmt1() {
        return amt1;
    }

    public Double getAmt2() {
        return amt2;
    }

    public Double getAmt3() {
        return amt3;
    }

    public String getCd() {
        return cd;
    }

    public Boolean getFlag() {
        return flag;
    }

    public PaymentStatus getStat() {
        return stat;
    }

    public LocalDateTime getCdt() {
        return cdt;
    }

    public LocalDateTime getUdt() {
        return udt;
    }

    /**
     * [Setter 메서드]
     * - 상태와 수정일시만 변경 가능하도록 제한
     * - 금액, 국가 등은 한 번 생성 후 변경 불가 (불변성)
     *
     * [왜 일부만 Setter를 제공하나?]
     * - 모든 필드에 Setter를 열면 어디서든 수정 가능
     * - 의도치 않은 수정으로 버그 발생 가능
     * - 비즈니스 로직에서만 필요한 변경만 허용
     */
    public void setStat(PaymentStatus stat) {
        this.stat = stat;
    }

    public void setUdt(LocalDateTime udt) {
        this.udt = udt;
    }
}
