# DDD 레이어드 아키텍처 (단순화 버전)

## 개요

이 패키지(`com.example.payment_ddd_v1`)는 **DDD 레이어드 아키텍처**를 학습하기 위한 단순화된 버전.


---

## 패키지 구조

```
com.example.payment_ddd_v1/
│
├── PaymentDddV1Application.java      # Spring Boot 메인 클래스
│
├── domain/                           # [1] 도메인 계층
│   ├── model/
│   │   ├── Payment.java              # 엔티티 (Rich Domain Model)
│   │   ├── PaymentStatus.java        # 상태 열거형
│   │   ├── Money.java                # Value Object
│   │   └── Country.java              # Value Object
│   ├── policy/
│   │   ├── DiscountPolicy.java       # 할인 정책 인터페이스
│   │   ├── VipDiscountPolicy.java    # 할인 정책 구현체
│   │   ├── TaxPolicy.java            # 세금 정책 인터페이스
│   │   └── KoreaTaxPolicy.java       # 세금 정책 구현체
│   └── repository/
│       └── PaymentRepository.java    # 저장소 인터페이스
│
├── application/                      # [2] 애플리케이션 계층
│   └── PaymentService.java           # 유스케이스 조율
│
├── infrastructure/                   # [3] 인프라스트럭처 계층
│   └── JpaPaymentRepository.java     # 저장소 구현체
│
└── interfaces/                       # [4] 인터페이스 계층
    ├── PaymentController.java        # REST API
    └── PaymentDto.java               # 요청/응답 DTO
```

---

## 4계층 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                    Interfaces (인터페이스)                    │
│                    - Controller, DTO                         │
│                    - 외부 요청 처리                           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Application (애플리케이션)                  │
│                   - Service                                  │
│                   - 유스케이스 조율, 트랜잭션                  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Domain (도메인)                         │
│                      - Entity, Value Object                  │
│                      - Policy, Repository Interface          │
│                      - 비즈니스 로직의 핵심                   │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │
┌─────────────────────────────────────────────────────────────┐
│                  Infrastructure (인프라)                     │
│                  - Repository 구현체                         │
│                  - 외부 시스템 연동                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 의존성 방향

```
Interfaces → Application → Domain ← Infrastructure
```

- **Domain은 어떤 계층에도 의존하지 않음** (핵심!)
- Infrastructure가 Domain에 의존 (의존성 역전)
- 화살표 방향 = 의존 방향

---

## 각 계층별 역할

### 1. Domain (도메인 계층)

**위치:** `domain/`

**역할:**
- 비즈니스 로직의 핵심
- 기술에 독립적 (Spring, JPA 등에 의존하지 않음)

**구성 요소:**

| 구성 요소 | 파일 | 설명 |
|----------|------|------|
| Entity | `Payment.java` | 비즈니스 로직을 포함한 도메인 객체 |
| Value Object | `Money.java` | 불변 객체, 금액 표현 |
| Value Object | `Country.java` | 불변 객체, 국가 코드 검증 |
| Policy | `DiscountPolicy.java` | 비즈니스 규칙 캡슐화 |
| Repository | `PaymentRepository.java` | 저장소 인터페이스 (구현 아님!) |

**핵심 코드:**

```java
// Entity - 비즈니스 로직 포함
public class Payment {
    public void complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("대기 상태만 완료 가능");
        }
        this.status = PaymentStatus.COMPLETED;
    }
}

// Value Object - 불변, 자가 검증
public class Money {
    private final double amount;

    private Money(double amount) {
        if (amount < 0) throw new IllegalArgumentException("음수 불가");
        this.amount = amount;
    }
}

// Value Object - 유효한 값만 허용
public class Country {
    private static final Set<String> SUPPORTED = Set.of("KR", "US");
    private final String code;

    private Country(String code) {
        if (!SUPPORTED.contains(code.toUpperCase())) {
            throw new IllegalArgumentException("지원하지 않는 국가");
        }
        this.code = code.toUpperCase();
    }
}
```

---

### 2. Application (애플리케이션 계층)

**위치:** `application/`

**역할:**
- 유스케이스 조율 (Orchestration)
- 트랜잭션 경계 관리
- 도메인 객체 조합

**주의:**
- 비즈니스 로직은 Domain에 위임
- 여기서는 흐름만 제어

**핵심 코드:**

```java
@Service
@Transactional
public class PaymentService {

    public Payment createPayment(...) {
        // 1. 할인 적용 (Policy 사용)
        Money discounted = discountPolicy.apply(originalPrice, isVip);

        // 2. 세금 적용 (Policy 사용)
        Money taxed = taxPolicy.apply(discounted);

        // 3. 결제 생성 (Entity 팩토리)
        Payment payment = Payment.create(...);

        // 4. 완료 처리 (Entity 메서드)
        payment.complete();

        // 5. 저장 (Repository)
        return paymentRepository.save(payment);
    }
}
```

---

### 3. Infrastructure (인프라스트럭처 계층)

**위치:** `infrastructure/`

**역할:**
- Domain 인터페이스의 구현
- 데이터베이스, 외부 시스템 연동

**핵심 코드:**

```java
@Repository
public class JpaPaymentRepository implements PaymentRepository {

    // Domain의 PaymentRepository 인터페이스 구현
    @Override
    public Payment save(Payment payment) {
        // 실제 저장 로직
    }
}
```

**왜 이렇게 분리하나요?**
- Domain이 DB 기술에 의존하지 않음
- JPA → MongoDB 변경 시 이 파일만 수정
- 테스트 시 Mock으로 쉽게 교체

---

### 4. Interfaces (인터페이스 계층)

**위치:** `interfaces/`

**역할:**
- 외부 요청 처리 (REST API, GraphQL, CLI 등)
- DTO 변환
- HTTP 상태 코드 결정

**핵심 코드:**

```java
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @PostMapping
    public ResponseEntity<PaymentDto.Response> createPayment(
            @RequestBody PaymentDto.Request request) {

        Payment payment = paymentService.createPayment(...);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaymentDto.Response.from(payment));
    }
}
```

---

## 핵심 패턴 요약

| 패턴 | 파일 | 설명 |
|------|------|------|
| Rich Domain Model | `Payment.java` | 엔티티가 비즈니스 로직 보유 |
| Value Object | `Money.java`, `Country.java` | 불변 객체, 값 동등성, 자가 검증 |
| Policy | `DiscountPolicy.java` | 비즈니스 규칙 캡슐화 |
| Repository | `PaymentRepository.java` | 저장소 추상화 |
| DIP | Domain ← Infrastructure | 의존성 역전 |

---

## 실행 방법

```bash
./gradlew bootRun -PmainClass=com.example.payment_ddd_v1.PaymentDddV1Application
```

---

## API 엔드포인트

| 메서드 | URL | 설명 |
|--------|-----|------|
| POST | /api/v1/payments | 결제 생성 |
| GET | /api/v1/payments/{id} | 결제 조회 |
| GET | /api/v1/payments | 전체 조회 |
| PATCH | /api/v1/payments/{id}/refund | 환불 |

---

## 요청/응답 예시

### 결제 생성 요청

```json
POST /api/v1/payments
{
  "amount": 10000,
  "country": "KR",
  "isVip": true
}
```

### 응답

```json
{
  "id": 1,
  "originalPrice": 10000,
  "discountedAmount": 8500,
  "taxedAmount": 9350,
  "country": "KR",
  "isVip": true,
  "status": "COMPLETED"
}
```

---

## 다음 단계 (심화)

이 패키지에서 다루지 않은 DDD 패턴:

| 패턴 | 설명 | 복잡도 |
|------|------|--------|
| Domain Event | 도메인 이벤트 발행/구독 | 중 |
| Aggregate | 트랜잭션 일관성 경계 | 중 |
| CQRS | 명령/조회 분리 | 상 |
| Event Sourcing | 이벤트 기반 상태 관리 | 상 |

심화 버전은 `com.example.payment_ddd` 패키지를 참고.
