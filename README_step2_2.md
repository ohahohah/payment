# Payment Step 2_2 - Aggregate 패턴 적용

## 개요

`payment_step1`에 Aggregate 패턴을 적용한 버전.
Payment Entity가 Rich Domain Model로 변경되어 비즈니스 로직을 캡슐화.

## payment_step1과의 차이점

### 핵심 변경사항

| 항목 | payment_step1 (Anemic) | payment_step2_2 (Rich) |
|------|------------------------|------------------------|
| Entity | setter로 상태 변경 | 비즈니스 메서드로 상태 변경 |
| Service | 상태 검증 로직 포함 | 위임(delegation)만 수행 |
| 테스트 초점 | Service 테스트가 핵심 | Entity 테스트가 핵심 |

### 상태 변경 코드 비교

#### payment_step1 (Anemic Domain Model)

```java
// PaymentService.java
public Payment refundPayment(Long id) {
    Payment payment = getPayment(id);

    // Service에서 상태 검증
    if (payment.getStatus() != PaymentStatus.COMPLETED) {
        throw new IllegalStateException("완료된 결제만 환불할 수 있습니다");
    }

    // setter로 상태 변경
    payment.setStatus(PaymentStatus.REFUNDED);
    payment.setUpdatedAt(LocalDateTime.now());

    return payment;
}
```

#### payment_step2_2 (Rich Domain Model)

```java
// PaymentService.java
public Payment refundPayment(Long id) {
    Payment payment = getPayment(id);
    payment.refund();  // Entity에 위임 (검증 + 상태 변경)
    return payment;
}

// Payment.java (Entity)
public void refund() {
    if (this.status != PaymentStatus.COMPLETED) {
        throw new IllegalStateException(
            "완료된 결제만 환불할 수 있습니다. 현재 상태: " + this.status);
    }
    this.status = PaymentStatus.REFUNDED;
    this.updatedAt = LocalDateTime.now();
}
```

## 변경된 파일 목록

### 1. Payment Entity (`entity/Payment.java`)

**주요 변경:**
- `setStatus()`, `setUpdatedAt()` setter 제거
- `complete()`, `fail()`, `refund()` 비즈니스 메서드 추가
- 상태 전이 규칙이 Entity 내부에 캡슐화

```java
// setter 없음!
// 비즈니스 메서드만 제공

public void complete() {
    if (this.status != PaymentStatus.PENDING) {
        throw new IllegalStateException("...");
    }
    this.status = PaymentStatus.COMPLETED;
    this.updatedAt = LocalDateTime.now();
}

public void fail() {
    if (this.status != PaymentStatus.PENDING) {
        throw new IllegalStateException("...");
    }
    this.status = PaymentStatus.FAILED;
    this.updatedAt = LocalDateTime.now();
}

public void refund() {
    if (this.status != PaymentStatus.COMPLETED) {
        throw new IllegalStateException("...");
    }
    this.status = PaymentStatus.REFUNDED;
    this.updatedAt = LocalDateTime.now();
}
```

### 2. PaymentService (`service/PaymentService.java`)

**주요 변경:**
- 상태 검증 로직 제거 (Entity로 이동)
- setter 호출 제거
- Entity의 비즈니스 메서드 호출로 단순화

```java
// 변경 전 (payment_step1)
public Payment completePayment(Long id) {
    Payment payment = getPayment(id);
    payment.setStatus(PaymentStatus.COMPLETED);
    payment.setUpdatedAt(LocalDateTime.now());
    return payment;
}

// 변경 후 (payment_step2_2)
public Payment completePayment(Long id) {
    Payment payment = getPayment(id);
    payment.complete();  // 단순 위임
    return payment;
}
```

### 3. 테스트 코드 변경

#### PaymentTest (Entity 테스트) - 핵심 테스트로 변경

```java
// 상태 전이 규칙 테스트
@Test
void shouldThrowExceptionWhenRefundFromPending() {
    Payment payment = createTestPayment();

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> payment.refund()  // Entity에서 예외 발생
    );

    assertTrue(exception.getMessage().contains("완료된 결제만 환불할 수 있습니다"));
}
```

#### PaymentServiceTest (Service 테스트) - 위임 확인으로 단순화

```java
// Service는 위임만 확인
@Test
void shouldDelegateToEntityRefund() {
    Payment payment = createTestPayment();
    payment.complete();
    given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

    Payment result = paymentService.refundPayment(1L);

    // Entity의 refund()가 호출되어 상태가 변경됨을 확인
    assertEquals(PaymentStatus.REFUNDED, result.getStatus());
}
```

## 상태 전이 다이어그램

```
                  complete()
    ┌─────────┐ ──────────────> ┌───────────┐
    │ PENDING │                 │ COMPLETED │
    └─────────┘                 └───────────┘
         │                            │
         │ fail()                     │ refund()
         │                            │
         ▼                            ▼
    ┌────────┐                  ┌──────────┐
    │ FAILED │                  │ REFUNDED │
    └────────┘                  └──────────┘
```

## Aggregate 패턴의 장점

### 1. 비즈니스 로직 중앙화
- 상태 전이 규칙이 Entity에 집중
- 여러 Service에서 사용해도 일관성 보장

### 2. 불변식(Invariant) 보호
- setter가 없으므로 외부에서 무효한 상태로 변경 불가
- Entity가 항상 유효한 상태 유지

### 3. 테스트 용이성
- Entity 단위 테스트로 비즈니스 규칙 검증
- Service 테스트는 단순한 위임 확인

### 4. 유지보수성
- 규칙 변경 시 Entity만 수정
- Service, Controller는 수정 불필요

## 파일 구조

```
payment_step2_2/
├── PaymentStep2_2Application.java
├── controller/
│   └── PaymentController.java
├── converter/
│   ├── CountryConverter.java
│   └── MoneyConverter.java
├── domain/
│   ├── model/
│   │   ├── Country.java          (Value Object)
│   │   └── Money.java            (Value Object)
│   └── policy/
│       ├── CustomerDiscountPolicy.java
│       ├── DiscountPolicy.java
│       ├── KoreaVatPolicy.java
│       ├── TaxPolicy.java
│       └── VipDiscountPolicy.java
├── dto/
│   ├── PaymentRequest.java
│   └── PaymentResult.java
├── entity/
│   ├── Payment.java              // Aggregate Root (Rich Domain Model)
│   └── PaymentStatus.java
├── handler/
│   └── GlobalExceptionHandler.java
├── repository/
│   └── PaymentRepository.java
└── service/
    └── PaymentService.java       // 비즈니스 로직 제거됨

test/
└── payment_step2_2/
    └── unit/
        ├── entity/
        │   └── PaymentTest.java   // 핵심 테스트 (상태 전이 규칙)
        └── service/
            └── PaymentServiceTest.java
```

## 테스트 실행

```bash
# Entity 테스트 (핵심)
./gradlew test --tests "com.example.payment_step2_2.unit.entity.*"

# Service 테스트
./gradlew test --tests "com.example.payment_step2_2.unit.service.*"

# 전체 테스트
./gradlew test --tests "com.example.payment_step2_2.*"
```

## Anemic vs Rich Domain Model 비교 요약

| 관점 | Anemic (step1) | Rich (step2_2) |
|------|----------------|----------------|
| Entity 역할 | 데이터 홀더 (getter/setter) | 비즈니스 로직 캡슐화 |
| Service 역할 | 비즈니스 로직 처리 | 조율(Orchestration) |
| 검증 로직 | Service에 분산 | Entity에 집중 |
| setter | 있음 (외부 변경 허용) | 없음 (메서드로만 변경) |
| 테스트 핵심 | Service 테스트 | Entity 테스트 |
