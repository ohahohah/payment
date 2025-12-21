# Step 1: Value Object와 Rich Domain Model

## 개요

이 단계에서는 Anti-DDD(빈약한 도메인 모델)에서 DDD(풍부한 도메인 모델)로 전환하는 첫 번째 단계를 다룹니다.

**핵심 변경 사항:**
- Primitive 타입 → Value Object (Money, Country)
- Anemic Entity → Rich Entity (Payment)
- setter 기반 상태 변경 → 비즈니스 메서드 기반 상태 변경
- 도메인 이벤트 도입

---

## 패키지 구조

```
com.example.payment_step1
└── domain
    ├── model
    │   ├── PaymentStatus.java    # 결제 상태 열거형
    │   ├── Money.java            # 금액 Value Object
    │   ├── Country.java          # 국가 Value Object
    │   └── Payment.java          # Aggregate Root (Rich Domain Model)
    └── event
        ├── DomainEvent.java           # 도메인 이벤트 인터페이스
        ├── PaymentCompletedEvent.java # 결제 완료 이벤트
        └── PaymentRefundedEvent.java  # 환불 이벤트
```

---

## Anti-DDD vs DDD 비교

### 1. Primitive Obsession vs Value Object

#### Anti-DDD (Primitive Obsession)

```java
public class Payment {
    private double originalPrice;      // 원시 타입
    private double discountedAmount;   // 타입만 보고는 의미 파악 불가
    private double taxedAmount;
    private String countryCode;        // 유효성 검증 어디서?

    // setter로 아무 값이나 설정 가능
    public void setOriginalPrice(double price) {
        this.originalPrice = price;  // 음수도 가능!
    }
}
```

**문제점:**
- 음수 금액 허용
- 잘못된 국가 코드 허용
- 타입만으로 의미 파악 불가
- 유효성 검증 로직이 여러 곳에 분산

#### DDD (Value Object)

```java
public class Payment {
    private final Money originalPrice;      // 의미 있는 타입!
    private final Money discountedAmount;
    private final Money taxedAmount;
    private final Country country;

    // setter 없음! 생성 시 검증 완료
}
```

**장점:**
- 자가 검증 (음수 불가)
- 타입만 봐도 의미 파악
- 불변 객체
- 단위 테스트 용이

---

### 2. Money Value Object

```java
public class Money {
    private final double amount;

    // 팩토리 메서드 - 자가 검증
    public static Money of(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다");
        }
        return new Money(amount);
    }

    // 불변 연산 - 새 객체 반환
    public Money multiply(double rate) {
        return new Money(Math.round(this.amount * rate));
    }

    public Money add(Money other) {
        return new Money(this.amount + other.amount);
    }

    // 비즈니스 메서드
    public boolean isGreaterThan(Money other) {
        return this.amount > other.amount;
    }
}
```

**Value Object 특징:**
1. **불변성**: 한번 생성되면 값 변경 불가
2. **자가 검증**: 유효하지 않은 값으로 생성 불가
3. **값 동등성**: 같은 값이면 equals() == true
4. **부수효과 없음**: 연산 시 새 객체 반환

---

### 3. Country Value Object

```java
public class Country {
    private static final Set<String> SUPPORTED = Set.of("KR", "US");
    private final String code;

    // 팩토리 메서드 - 지원 국가만 허용
    public static Country of(String code) {
        String normalized = code.toUpperCase();
        if (!SUPPORTED.contains(normalized)) {
            throw new IllegalArgumentException("지원하지 않는 국가: " + code);
        }
        return new Country(normalized);
    }

    // 비즈니스 메서드 - 도메인 지식 캡슐화
    public boolean isKorea() { return "KR".equals(code); }
    public boolean isUs() { return "US".equals(code); }

    // 편의 팩토리 메서드
    public static Country korea() { return of("KR"); }
    public static Country us() { return of("US"); }
}
```

---

### 4. Anemic vs Rich Domain Model

#### Anti-DDD (Anemic Domain Model)

```java
// Entity - 데이터만 보관
public class Payment {
    private PaymentStatus status;

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) {
        this.status = status;  // 아무 상태로나 변경 가능!
    }
}

// Service - 비즈니스 로직 담당
public class PaymentService {
    public void complete(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("...");
        }
        payment.setStatus(PaymentStatus.COMPLETED);
        // 정산 서비스 호출, 로깅 등...
    }
}
```

**문제점:**
- Entity가 단순 데이터 홀더
- 비즈니스 규칙이 Service에 흩어짐
- 외부에서 setStatus() 직접 호출 가능
- 도메인 지식 파편화

#### DDD (Rich Domain Model)

```java
public class Payment {
    private PaymentStatus status;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // setter 없음! 비즈니스 메서드로만 상태 변경

    public void complete() {
        // 도메인 규칙 검증
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                "대기 상태의 결제만 완료할 수 있습니다");
        }

        // 상태 변경
        this.status = PaymentStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();

        // 도메인 이벤트 등록
        registerEvent(new PaymentCompletedEvent(this.id, this.taxedAmount));
    }

    public void refund() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                "완료된 결제만 환불할 수 있습니다");
        }

        this.status = PaymentStatus.REFUNDED;
        registerEvent(new PaymentRefundedEvent(this.id, this.taxedAmount));
    }
}
```

**장점:**
- 도메인 규칙이 Entity 안에 캡슐화
- 잘못된 상태 전이 불가능
- 의미 있는 메서드명 (complete, refund)
- 도메인 이벤트 자동 등록

---

### 5. 상태 전이 다이어그램

```
                    complete()
    ┌─────────────┐ ─────────────> ┌─────────────┐
    │   PENDING   │                │  COMPLETED  │
    └─────────────┘                └─────────────┘
           │                              │
           │ fail()                       │ refund()
           ▼                              ▼
    ┌─────────────┐                ┌─────────────┐
    │   FAILED    │                │  REFUNDED   │
    └─────────────┘                └─────────────┘
```

**상태 전이 규칙:**
- PENDING → COMPLETED (complete 호출)
- PENDING → FAILED (fail 호출)
- COMPLETED → REFUNDED (refund 호출)
- 그 외 전이는 모두 예외 발생!

---

### 6. 도메인 이벤트

```java
// 도메인 이벤트 인터페이스
public interface DomainEvent {
    LocalDateTime occurredAt();
}

// 결제 완료 이벤트 - record로 불변 보장
public record PaymentCompletedEvent(
    Long paymentId,
    Money finalAmount,
    LocalDateTime occurredAt
) implements DomainEvent {

    public PaymentCompletedEvent(Long paymentId, Money finalAmount) {
        this(paymentId, finalAmount, LocalDateTime.now());
    }
}
```

**도메인 이벤트 특징:**
1. **과거형 이름**: "~Completed", "~Refunded"
2. **불변 객체**: record 또는 final 필드
3. **발생 시각 포함**: 언제 일어났는지 기록
4. **Aggregate Root에서 등록**: Entity 내부에서 이벤트 생성

---

## 테스트 코드

### Value Object 테스트

```java
@Test
@DisplayName("음수 금액은 거부 - 자가 검증")
void rejectNegativeAmount() {
    assertThatThrownBy(() -> Money.of(-1000))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("0 이상");
}

@Test
@DisplayName("같은 금액은 동등 (equals)")
void equalsByValue() {
    Money money1 = Money.of(10000);
    Money money2 = Money.of(10000);

    // 다른 인스턴스지만 값이 같으면 같다!
    assertThat(money1).isEqualTo(money2);
}
```

### Rich Domain Model 테스트

```java
@Test
@DisplayName("완료된 결제에 complete() 호출 → 예외")
void cannotCompleteAlreadyCompleted() {
    Payment payment = createSamplePayment();
    payment.complete();  // 첫 번째 완료

    // 두 번째 완료 시도 → 예외!
    assertThatThrownBy(() -> payment.complete())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("대기 상태");
}

@Test
@DisplayName("complete() 호출 시 PaymentCompletedEvent 발생")
void emitEventOnComplete() {
    Payment payment = createSamplePayment();

    payment.complete();

    List<DomainEvent> events = payment.getDomainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(PaymentCompletedEvent.class);
}
```

---

## 테스트 실행

```bash
# Step 1 테스트만 실행
./gradlew test --tests "com.example.payment_step1.*"
```

---

## 핵심 포인트 정리

| 항목 | Anti-DDD | DDD (Step 1) |
|------|----------|--------------|
| 금액 타입 | `double` | `Money` (Value Object) |
| 국가 타입 | `String` | `Country` (Value Object) |
| 상태 변경 | `setStatus()` | `complete()`, `refund()` |
| 규칙 검증 | Service에서 | Entity에서 |
| 불변성 | X (setter 존재) | O (setter 없음) |
| 도메인 이벤트 | X | O |

---

## 다음 단계 (Step 2)

Step 2에서는 Application Layer와 Infrastructure Layer를 추가합니다:
- Repository 인터페이스와 구현체
- Application Service
- 이벤트 발행 처리

현재 Step 1에서는 도메인 모델만 변경했으며, 이것만으로도:
- 타입 안전성 확보
- 비즈니스 규칙 캡슐화
- 테스트 용이성 향상

의 효과를 얻을 수 있습니다.
