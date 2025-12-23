# Payment DDD V1_1 - 정석 DDD 계층형 아키텍처

## 개요

**정석 DDD** 구조를 따르는 결제 시스템.
Domain 계층이 프레임워크(JPA)에 전혀 의존하지 않음.

---

## payment_ddd_v1 vs payment_ddd_v1_1

| 구분 | payment_ddd_v1 (실용적) | payment_ddd_v1_1 (정석) |
|------|------------------------|------------------------|
| Domain Payment | `@Entity` 있음 | 순수 Java |
| JPA 의존성 | Domain에 침투 | Infrastructure에만 |
| 추가 클래스 | 없음 | PaymentJpaEntity, Mapper |
| 변환 비용 | 없음 | 있음 |
| 코드량 | 적음 | 많음 |

---

## 패키지 구조

```
payment_ddd_v1_1/
├── interfaces/                        # 사용자 인터페이스 계층
│   ├── PaymentController.java
│   ├── PaymentRequest.java
│   └── PaymentResponse.java
│
├── application/                       # 응용 서비스 계층
│   └── PaymentService.java
│
├── domain/                            # 도메인 계층 (순수 Java)
│   ├── model/
│   │   ├── Payment.java              # @Entity 없음
│   │   ├── PaymentStatus.java
│   │   ├── Money.java
│   │   └── Country.java
│   ├── policy/
│   │   ├── DiscountPolicy.java
│   │   ├── CustomerDiscountPolicy.java
│   │   ├── VipDiscountPolicy.java
│   │   ├── TaxPolicy.java
│   │   └── KoreaTaxPolicy.java
│   └── repository/
│       └── PaymentRepository.java     # 인터페이스
│
└── infrastructure/                    # 인프라 계층 (JPA 의존)
    ├── persistence/
    │   ├── PaymentJpaEntity.java     # @Entity 있음
    │   ├── JpaPaymentRepository.java
    │   └── SpringDataPaymentRepository.java
    └── mapper/
        └── PaymentMapper.java        # Domain ↔ JPA 변환
```

---

## 핵심 구조

### 1. Domain 계층 (순수 Java)

```java
// Payment.java - @Entity 없음
public class Payment {
    private Long id;
    private Money originalPrice;      // Value Object
    private Country country;          // Value Object
    private PaymentStatus status;

    public void complete() { ... }    // 비즈니스 로직
    public void refund() { ... }
}
```

### 2. Infrastructure 계층 (JPA 의존)

```java
// PaymentJpaEntity.java - @Entity 있음
@Entity
@Table(name = "payments_ddd_v1_1")
public class PaymentJpaEntity {
    @Id
    private Long id;

    @Column
    private Double originalPrice;     // 원시 타입

    @Column
    private String country;           // 원시 타입
}
```

### 3. Mapper (변환)

```java
// PaymentMapper.java
@Component
public class PaymentMapper {

    // JPA Entity → Domain
    public Payment toDomain(PaymentJpaEntity entity) {
        return Payment.reconstitute(
            entity.getId(),
            Money.of(entity.getOriginalPrice()),
            Country.of(entity.getCountry()),
            ...
        );
    }

    // Domain → JPA Entity
    public PaymentJpaEntity toEntity(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setOriginalPrice(payment.getOriginalPrice().getAmount());
        entity.setCountry(payment.getCountry().getCode());
        ...
        return entity;
    }
}
```

### 4. Repository (변환 적용)

```java
// JpaPaymentRepository.java
@Repository
public class JpaPaymentRepository implements PaymentRepository {

    private final SpringDataPaymentRepository jpaRepo;
    private final PaymentMapper mapper;

    @Override
    public Payment save(Payment payment) {
        // Domain → JPA Entity → 저장 → JPA Entity → Domain
        PaymentJpaEntity entity = mapper.toEntity(payment);
        PaymentJpaEntity saved = jpaRepo.save(entity);
        return mapper.toDomain(saved);
    }
}
```

---

## 실행 방법

```bash
cd payment-processor
./gradlew bootRun -PmainClass=com.example.payment_ddd_v1_1.PaymentDddV1_1Application
```

---

## API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /api/ddd/v1_1/payments | 결제 생성 |
| GET | /api/ddd/v1_1/payments/{id} | 결제 조회 |
| GET | /api/ddd/v1_1/payments | 전체 조회 |
| POST | /api/ddd/v1_1/payments/{id}/complete | 결제 완료 |
| POST | /api/ddd/v1_1/payments/{id}/refund | 환불 |
| POST | /api/ddd/v1_1/payments/{id}/fail | 실패 |

---

## 장단점

### 장점

1. **Domain 순수성**
   - 기술 변경에 영향받지 않음
   - JPA 없이 단위 테스트 가능

2. **유연성**
   - JPA → MongoDB 교체 시 Domain 수정 없음
   - Infrastructure만 교체

3. **테스트 용이성**
   - Domain 테스트에 Spring Context 불필요

### 단점

1. **코드량 증가**
   - PaymentJpaEntity 추가
   - PaymentMapper 추가

2. **변환 비용**
   - 매 조회/저장 시 객체 변환 발생
   - 대용량 처리 시 성능 고려 필요

3. **복잡도**
   - 이해해야 할 클래스 증가
   - 디버깅 시 추적 복잡

---

## 언제 사용?

| 상황 | 추천 |
|------|------|
| 스타트업, 빠른 개발 | payment_ddd_v1 (실용적) |
| 대기업, 장기 프로젝트 | payment_ddd_v1_1 (정석) |
| 기술 변경 가능성 높음 | payment_ddd_v1_1 |
| 팀이 DDD에 익숙함 | payment_ddd_v1_1 |
| 팀이 DDD 처음 | payment_ddd_v1 |

---

## 테스트

### 테스트 구조

```
src/test/java/com/example/payment_ddd_v1_1/
├── domain/                                    # 순수 Java 테스트 (JPA 없음)
│   ├── model/
│   │   ├── MoneyTest.java                    # Money VO 단위 테스트
│   │   └── PaymentTest.java                  # Payment 엔티티 단위 테스트
│   └── policy/
│       └── DiscountPolicyTest.java           # 할인/세금 정책 테스트
├── infrastructure/
│   └── mapper/
│       └── PaymentMapperTest.java            # Domain ↔ JPA 변환 테스트
├── application/
│   └── PaymentServiceTest.java               # 서비스 단위 테스트 (Mock)
└── PaymentDddV1_1IntegrationTest.java        # 통합 테스트
```

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 패키지 테스트만 실행
./gradlew test --tests "com.example.payment_ddd_v1_1.*"

# 순수 도메인 테스트만 실행 (JPA 없이)
./gradlew test --tests "com.example.payment_ddd_v1_1.domain.*"

# Mapper 테스트만 실행
./gradlew test --tests "com.example.payment_ddd_v1_1.infrastructure.mapper.*"

# 통합 테스트만 실행
./gradlew test --tests "com.example.payment_ddd_v1_1.PaymentDddV1_1IntegrationTest"
```

### 정석 DDD 테스트의 핵심 장점

| 특징 | 설명 |
|------|------|
| JPA 없이 도메인 테스트 | Payment, Money 테스트에 @Entity 불필요 |
| Spring Context 불필요 | 도메인 테스트는 순수 Java만으로 실행 |
| 매우 빠른 실행 | 밀리초 단위 테스트 실행 |
| 도메인 로직 집중 | 인프라 걱정 없이 비즈니스 규칙만 테스트 |

### 테스트 유형

| 유형 | 설명 | 특징 |
|------|------|------|
| Domain 단위 테스트 | Payment, Money 테스트 | **JPA 없이 순수 Java** |
| Mapper 테스트 | Domain ↔ JPA Entity 변환 | 데이터 무결성 검증 |
| Service 단위 테스트 | Mock Repository 사용 | Spring 불필요 |
| 통합 테스트 | 전체 계층 테스트 | Spring Context, H2 DB |

### 주요 테스트 케이스

**PaymentTest (도메인 테스트 - 순수 Java)**
- `create()` - 새 결제 생성 테스트
- `reconstitute()` - 기존 결제 복원 테스트 (Repository용)
- `assignId()` - ID 할당 테스트
- 상태 전이 테스트 (PENDING → COMPLETED → REFUNDED)
- 비즈니스 규칙 검증 (잘못된 상태 전이 예외)

**MoneyTest (Value Object 테스트)**
- 생성 및 검증 (음수 금액 예외)
- 불변성 테스트 (원본 객체 보존)
- 연산 테스트 (add, subtract, multiply)
- 동등성 테스트 (Value Equality)

**PaymentMapperTest (변환 테스트)**
- `toDomain()` - JPA Entity → Domain 변환
- `toEntity()` - Domain → JPA Entity 변환
- 왕복 변환 테스트 (Round-trip) - 데이터 보존 확인
- 모든 상태 변환 테스트 (PENDING, COMPLETED, FAILED, REFUNDED)

**PaymentServiceTest (서비스 테스트)**
- 일반/VIP 결제 생성
- 할인율 적용 검증 (10%, 20%)
- Repository 협력 검증

**PaymentDddV1_1IntegrationTest (통합 테스트)**
- Domain → JPA Entity → Domain 변환 검증
- 결제 생성 및 DB 저장
- 상태 변경 및 영속화
- 예외 케이스

### payment_ddd_v1 테스트와의 차이점

| 관점 | payment_ddd_v1 | payment_ddd_v1_1 |
|------|----------------|------------------|
| 도메인 테스트 | @Entity 있어도 동작 | **JPA 전혀 불필요** |
| Mapper 테스트 | 없음 | **있음 (필수)** |
| 테스트 순수성 | 보통 | **매우 높음** |
| 변환 검증 | 불필요 | **Round-trip 테스트** |
