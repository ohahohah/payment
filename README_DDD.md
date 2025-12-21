# DDD 결제 시스템 (payment_ddd)

이 문서는 DDD(Domain-Driven Design) 패턴을 적용한 결제 시스템의 구조와 특징을 설명합니다.

## 패키지 구조

```
com.example.payment_ddd
├── domain/                          # 도메인 레이어 (핵심 비즈니스)
│   ├── model/                       # 도메인 모델
│   │   ├── Payment.java             # Aggregate Root
│   │   ├── PaymentStatus.java       # 상태 Enum
│   │   ├── Money.java               # Value Object (금액)
│   │   └── Country.java             # Value Object (국가)
│   ├── event/                       # 도메인 이벤트
│   │   ├── DomainEvent.java         # 이벤트 인터페이스
│   │   ├── PaymentCompletedEvent.java
│   │   └── PaymentRefundedEvent.java
│   ├── policy/                      # 도메인 정책 (전략 패턴)
│   │   ├── DiscountPolicy.java      # 할인 정책 인터페이스
│   │   ├── VipDiscountPolicy.java   # VIP 할인 구현
│   │   ├── TaxPolicy.java           # 세금 정책 인터페이스
│   │   ├── KoreaTaxPolicy.java      # 한국 세금 구현
│   │   └── UsTaxPolicy.java         # 미국 세금 구현
│   ├── service/                     # 도메인 서비스
│   │   └── PaymentDomainService.java
│   └── repository/                  # 저장소 인터페이스
│       └── PaymentRepository.java
│
├── application/                     # 애플리케이션 레이어 (유스케이스)
│   ├── command/                     # 커맨드 객체
│   │   ├── CreatePaymentCommand.java
│   │   └── RefundPaymentCommand.java
│   ├── service/                     # 애플리케이션 서비스
│   │   └── PaymentCommandService.java
│   └── eventhandler/                # 이벤트 핸들러
│       ├── DomainEventHandler.java
│       ├── LoggingEventHandler.java
│       ├── SettlementEventHandler.java
│       └── RefundLoggingEventHandler.java
│
├── infrastructure/                  # 인프라스트럭처 레이어
│   ├── persistence/                 # 영속성
│   │   ├── PaymentJpaEntity.java    # JPA 엔티티
│   │   ├── PaymentJpaRepository.java
│   │   └── JpaPaymentRepository.java # Repository 구현
│   └── config/                      # 설정
│       └── PaymentDddConfig.java    # Bean 등록
│
└── interfaces/                      # 인터페이스 레이어
    ├── dto/                         # DTO
    │   ├── PaymentRequest.java
    │   └── PaymentResponse.java
    └── rest/                        # REST API
        └── PaymentDddController.java
```

## Anti-DDD vs DDD 비교

### 1. 도메인 모델

| 항목 | Anti-DDD (Anemic Model) | DDD (Rich Domain Model) |
|------|------------------------|-------------------------|
| 엔티티 역할 | 데이터 보관만 (getter/setter) | 비즈니스 로직 포함 |
| 상태 변경 | setter로 어디서든 변경 가능 | 의미있는 메서드로만 변경 (complete(), refund()) |
| 규칙 검증 | 서비스에서 검증 | 엔티티 내부에서 검증 |
| 도메인 이벤트 | 없음 | 상태 변경 시 이벤트 등록 |

**Anti-DDD 코드:**
```java
// 어디서든 상태 변경 가능
payment.setStatus(PaymentStatus.COMPLETED);
payment.setUpdatedAt(LocalDateTime.now());
```

**DDD 코드:**
```java
// 비즈니스 규칙이 엔티티 안에 캡슐화
public void complete() {
    if (this.status != PaymentStatus.PENDING) {
        throw new IllegalStateException("대기 상태의 결제만 완료할 수 있습니다");
    }
    this.status = PaymentStatus.COMPLETED;
    this.updatedAt = LocalDateTime.now();
    registerEvent(new PaymentCompletedEvent(this.id, this.taxedAmount));
}
```

### 2. 서비스 구조

| 항목 | Anti-DDD (Fat Service) | DDD (계층 분리) |
|------|----------------------|-----------------|
| 서비스 역할 | 모든 로직이 한 곳에 | 역할별 분리 |
| 테스트 | 전체 의존성 필요 | 순수 도메인 테스트 가능 |
| 확장성 | 수정 범위가 넓음 | 변경 범위 국소화 |

**Anti-DDD 구조:**
```
PaymentService
├── 할인 계산
├── 세금 계산
├── 상태 검증
├── 상태 변경
├── DB 저장
└── 로깅
```

**DDD 구조:**
```
Domain Layer
├── PaymentDomainService (할인, 세금 조합)
├── Payment (상태 변경, 규칙 검증)
├── DiscountPolicy (할인 정책)
└── TaxPolicy (세금 정책)

Application Layer
├── PaymentCommandService (유스케이스 조율)
└── EventHandlers (이벤트 처리)
```

### 3. Value Object

| 항목 | Anti-DDD | DDD |
|------|---------|-----|
| 금액 타입 | double | Money (Value Object) |
| 국가 타입 | String | Country (Value Object) |
| 유효성 검증 | 서비스에서 | 객체 생성 시 (자가 검증) |
| 비즈니스 메서드 | 없음 | 있음 (isKorea(), multiply()) |

**Anti-DDD:**
```java
double amount = 10000;  // 타입 안전성 없음
String country = "KR";  // 유효하지 않은 값 허용
```

**DDD:**
```java
Money amount = Money.of(10000);     // 음수 거부
Country country = Country.of("KR");  // 지원 국가만 허용
```

### 4. 의존성 방향

**Anti-DDD:**
```
Controller → Service → Repository
     ↓
   Entity (JPA 의존)
```

**DDD:**
```
Interfaces → Application → Domain ← Infrastructure
                 ↓
            Domain (순수 Java)
```

- Domain 레이어는 어떤 프레임워크에도 의존하지 않음
- Infrastructure가 Domain을 의존 (의존성 역전)

## 실행 방법

### 1. 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

### 2. API 테스트

**DDD 결제 API (v2):**
- Base URL: `http://localhost:8080/api/v2/payments`

**Anti-DDD 결제 API (v1):**
- Base URL: `http://localhost:8080/api/payments`

### 3. 테스트 실행

```bash
# 전체 테스트
./gradlew test

# DDD 패키지만 테스트
./gradlew test --tests "com.example.payment_ddd.*"
```

## DDD 주요 개념

### Aggregate Root
- `Payment`가 Aggregate Root
- 외부에서는 반드시 Payment를 통해서만 내부 상태에 접근
- 트랜잭션 일관성의 경계

### Value Object
- `Money`, `Country`
- 불변, 동등성(값으로 비교), 자가 검증
- 도메인 개념을 명확하게 표현

### Domain Event
- `PaymentCompletedEvent`, `PaymentRefundedEvent`
- 도메인에서 발생한 "과거의 사실"
- 느슨한 결합을 통한 확장성

### Domain Service
- `PaymentDomainService`
- 특정 엔티티에 속하지 않는 도메인 로직
- 여러 도메인 객체를 조합하는 비즈니스 로직

### Application Service
- `PaymentCommandService`
- 유스케이스 조율 (트랜잭션, 이벤트 발행)
- 도메인 로직을 포함하지 않음

### Repository
- `PaymentRepository` (인터페이스는 Domain에)
- `JpaPaymentRepository` (구현은 Infrastructure에)
- 의존성 역전 원칙 적용

## 테스트 전략

### 1. 도메인 테스트 (순수 Java)
```
MoneyTest, CountryTest, PaymentTest
DiscountPolicyTest, TaxPolicyTest
PaymentDomainServiceTest
```
- Spring 없이 빠른 실행
- 비즈니스 로직 검증

### 2. Application Service 테스트
```
PaymentCommandServiceTest
```
- InMemory Repository 사용
- 유스케이스 흐름 검증

### 3. 통합 테스트
```
PaymentDddIntegrationTest
```
- 실제 Spring Context
- 전체 흐름 검증

## 할인/세금 계산 예시

| 국가 | VIP 여부 | 원래 가격 | 할인 적용 | 세금 적용 | 최종 금액 |
|------|---------|-----------|-----------|-----------|-----------|
| 한국 | VIP | 10,000원 | 9,000원 (10% 할인) | 9,900원 (VAT 10%) | 9,900원 |
| 한국 | 일반 | 10,000원 | 10,000원 | 11,000원 (VAT 10%) | 11,000원 |
| 미국 | VIP | 10,000원 | 9,000원 (10% 할인) | 9,720원 (Tax 8%) | 9,720원 |
| 미국 | 일반 | 10,000원 | 10,000원 | 10,800원 (Tax 8%) | 10,800원 |
