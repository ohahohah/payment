# Payment DDD V1 - DDD 레이어드 아키텍처 예제

## 개요

DDD(Domain-Driven Design) 레이어드 아키텍처를 적용한 결제 시스템.

**주요 특징:**
- Rich Domain Model (비즈니스 로직이 Entity에 캡슐화)
- Value Object (Money, Country)
- Repository 패턴 (의존성 역전 - DIP)
- **JPA를 활용한 실제 데이터베이스 영속화 (H2)**

---

## 계층 구조

```
┌─────────────────────────────────────────────────────────────┐
│                    Interfaces 계층                          │
│  (PaymentController, PaymentDto)                            │
│  - 사용자 요청/응답 처리                                    │
└─────────────────────────────────────────────────────────────┘
                              ↓ 의존
┌─────────────────────────────────────────────────────────────┐
│                    Application 계층                          │
│  (PaymentService)                                            │
│  - 유스케이스 조율 (Orchestration)                           │
│  - 트랜잭션 관리                                             │
│  - 비즈니스 로직 없음 (Domain에 위임)                        │
└─────────────────────────────────────────────────────────────┘
                              ↓ 의존
┌─────────────────────────────────────────────────────────────┐
│                      Domain 계층                             │
│  model/    Payment, Money, Country, PaymentStatus           │
│  policy/   DiscountPolicy, TaxPolicy                        │
│  repository/  PaymentRepository (인터페이스)                 │
│                                                              │
│  - 핵심 비즈니스 로직                                        │
└─────────────────────────────────────────────────────────────┘
                              ↑ 구현
┌─────────────────────────────────────────────────────────────┐
│                   Infrastructure 계층                        │
│  JpaPaymentRepository, MoneyConverter, CountryConverter     │
│  - Repository 구현체 (Spring Data JPA)                       │
│  - Value Object ↔ DB 컬럼 변환 (Converter)                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 패키지 구조

```
payment_ddd_v1/
├── PaymentDddV1Application.java       # 메인 애플리케이션
│
├── interfaces/                         # 사용자 인터페이스 계층
│   ├── PaymentController.java         # REST API
│   └── PaymentDto.java                # 요청/응답 DTO
│
├── application/                        # 응용 서비스 계층
│   └── PaymentService.java            # 유스케이스 조율
│
├── domain/                             # 도메인 계층 (핵심!)
│   ├── model/
│   │   ├── Payment.java               # Aggregate Root (JPA Entity)
│   │   ├── PaymentStatus.java         # 상태 열거형
│   │   ├── Money.java                 # Value Object
│   │   └── Country.java               # Value Object
│   ├── policy/
│   │   ├── DiscountPolicy.java        # 할인 정책 인터페이스
│   │   ├── VipDiscountPolicy.java     # VIP 할인 구현
│   │   ├── TaxPolicy.java             # 세금 정책 인터페이스
│   │   └── KoreaTaxPolicy.java        # 한국 세금 구현
│   └── repository/
│       └── PaymentRepository.java     # Repository 인터페이스 (DIP)
│
└── infrastructure/                     # 인프라 계층
    ├── JpaPaymentRepository.java      # Repository 구현체
    ├── SpringDataPaymentRepository.java  # Spring Data JPA
    └── converter/
        ├── MoneyConverter.java        # Money -> DB Double
        └── CountryConverter.java      # Country -> DB String
```

---

## 실행 방법

```bash
# payment-processor 디렉토리에서
cd payment-processor

# 실행
./gradlew bootRun -PmainClass=com.example.payment_ddd_v1.PaymentDddV1Application
```

---

## H2 Console 접속

```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:paymentdb
Username: sa
Password: (비워둠)
```

**테이블 확인:**
```sql
SELECT * FROM payments_ddd_v1;
```

---

## API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /api/ddd/v1/payments | 결제 생성 |
| GET | /api/ddd/v1/payments/{id} | 결제 조회 |
| GET | /api/ddd/v1/payments | 전체 조회 |
| POST | /api/ddd/v1/payments/{id}/refund | 환불 처리 |

### 요청 예시

```bash
# 결제 생성
curl -X POST http://localhost:8080/api/ddd/v1/payments \
  -H "Content-Type: application/json" \
  -d '{"amount": 10000, "countryCode": "KR", "isVip": true}'

# 결제 조회
curl http://localhost:8080/api/ddd/v1/payments/1

# 환불
curl -X POST http://localhost:8080/api/ddd/v1/payments/1/refund
```

---

## JPA 영속화 구조

### Value Object 매핑 (@Convert)

```java
// Payment.java
@Entity
@Table(name = "payments_ddd_v1")
public class Payment {

    @Convert(converter = MoneyConverter.class)
    @Column(name = "original_price", nullable = false)
    private Money originalPrice;

    @Convert(converter = CountryConverter.class)
    @Column(nullable = false, length = 10)
    private Country country;
}
```

### Converter 구현

```java
// MoneyConverter.java
@Converter
public class MoneyConverter implements AttributeConverter<Money, Double> {

    @Override
    public Double convertToDatabaseColumn(Money money) {
        return money == null ? null : money.getAmount();
    }

    @Override
    public Money convertToEntityAttribute(Double amount) {
        return amount == null ? null : Money.of(amount);
    }
}
```

### Repository 계층 구조 (DIP)

```
Domain 계층:
    PaymentRepository (인터페이스)  ← PaymentService가 의존

Infrastructure 계층:
    JpaPaymentRepository (구현체)
         ↓ 위임
    SpringDataPaymentRepository (Spring Data JPA)
```

---

## 테이블 구조

```sql
CREATE TABLE payments_ddd_v1 (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_price DOUBLE NOT NULL,      -- Money VO
    discounted_amount DOUBLE NOT NULL,   -- Money VO
    taxed_amount DOUBLE NOT NULL,        -- Money VO
    country VARCHAR(10) NOT NULL,        -- Country VO
    vip BOOLEAN NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

---

## 핵심 DDD 개념

### 1. Rich Domain Model

```java
public class Payment {
    // 비즈니스 로직이 Entity 내부에 캡슐화
    public void complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("...");
        }
        this.status = PaymentStatus.COMPLETED;
    }

    public void refund() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("...");
        }
        this.status = PaymentStatus.REFUNDED;
    }
}
```

### 2. Value Object

```java
public class Money {
    private final double amount;  // 불변

    private Money(double amount) {
        if (amount < 0) {  // 자가 검증
            throw new IllegalArgumentException("...");
        }
        this.amount = amount;
    }

    public static Money of(double amount) {
        return new Money(amount);
    }

    // equals/hashCode 구현 (값 동등성)
}
```

### 3. 의존성 역전 (DIP)

```java
// Domain 계층 - 인터페이스만 정의
public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long id);
}

// Infrastructure 계층 - 구현
@Repository
public class JpaPaymentRepository implements PaymentRepository {
    private final SpringDataPaymentRepository springDataRepository;

    @Override
    public Payment save(Payment payment) {
        return springDataRepository.save(payment);
    }
}
```

---

## 상태 전이

```
              complete()
    PENDING ──────────────> COMPLETED
        │                       │
        │ fail()                │ refund()
        ▼                       ▼
     FAILED                 REFUNDED
```

---

## Anemic vs Rich Domain Model 비교

| 관점 | Anemic (payment_ul) | Rich (payment_ddd_v1) |
|------|---------------------|----------------------|
| Entity | 데이터만 보유 | 데이터 + 비즈니스 로직 |
| Service | 비즈니스 로직 처리 | 흐름 조율만 |
| 상태 변경 | setter 사용 | 비즈니스 메서드 |
| 테스트 초점 | Service 테스트 | Entity 테스트 |

---

## 테스트

### 테스트 구조

```
src/test/java/com/example/payment_ddd_v1/
├── domain/
│   ├── model/
│   │   ├── MoneyTest.java          # Money VO 단위 테스트
│   │   └── PaymentTest.java        # Payment 엔티티 단위 테스트
│   └── policy/
│       └── DiscountPolicyTest.java # 할인 정책 테스트
├── application/
│   └── PaymentServiceTest.java     # 서비스 단위 테스트 (Mock)
└── PaymentDddV1IntegrationTest.java # 통합 테스트
```

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 패키지 테스트만 실행
./gradlew test --tests "com.example.payment_ddd_v1.*"

# 단위 테스트만 실행
./gradlew test --tests "com.example.payment_ddd_v1.domain.*"

# 통합 테스트만 실행
./gradlew test --tests "com.example.payment_ddd_v1.PaymentDddV1IntegrationTest"
```

### 테스트 유형

| 유형 | 설명 | 특징 |
|------|------|------|
| Domain 단위 테스트 | Entity, Value Object 테스트 | Spring 불필요, 빠름 |
| Service 단위 테스트 | Mock Repository 사용 | Spring 불필요, 빠름 |
| 통합 테스트 | 전체 계층 테스트 | Spring Context, H2 DB |

### 주요 테스트 케이스

**PaymentTest (도메인 테스트)**
- 결제 생성 테스트
- 상태 전이 테스트 (PENDING → COMPLETED → REFUNDED)
- 비즈니스 규칙 검증 (잘못된 상태 전이 예외)

**MoneyTest (Value Object 테스트)**
- 생성 및 검증 (음수 금액 예외)
- 불변성 테스트
- 연산 테스트 (add, subtract, multiply)
- 동등성 테스트

**PaymentServiceTest (서비스 테스트)**
- 일반/VIP 결제 생성
- 할인율 적용 검증
- Repository 협력 검증

**PaymentDddV1IntegrationTest (통합 테스트)**
- 결제 생성 및 DB 저장
- 상태 변경 및 영속화
- 예외 케이스
