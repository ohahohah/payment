# Payment Step 1: Value Object 적용

## 개요

이 패키지(`com.example.payment_step1`)는 **Value Object 패턴만** 적용한 독립 실행 가능한 결제 시스템.
`payment_ul`의 `double`/`String` 타입을 `Money`/`Country` Value Object로 변경함.

**적용 범위:**
- Value Object (Money, Country)
- @Convert를 통한 JPA 매핑

**미적용 (payment_ul과 동일):**
- Entity는 Anemic Domain Model 유지
- setter를 통한 상태 변경
- 비즈니스 로직은 Service에서 처리

---

## payment_ul과의 차이점

| 항목 | payment_ul | payment_step1 |
|------|------------|---------------|
| 금액 타입 | `Double` | `Money` (Value Object) |
| 국가 타입 | `String` | `Country` (Value Object) |
| 금액 검증 | Service에서 if문 | Money 생성 시 자동 검증 |
| 국가 검증 | 없음 | Country 생성 시 자동 검증 |
| Policy 시그니처 | `double -> double` | `Money -> Money` |
| JPA 매핑 | 기본 타입 | `@Convert` 사용 |
| Entity 상태변경 | setter 사용 | setter 사용 (동일) |
| 비즈니스 로직 위치 | Service | Service (동일) |

---

## 패키지 구조

```
com.example.payment_step1/
|
+-- PaymentStep1Application.java     # Spring Boot 메인 클래스
|
+-- domain/
|   +-- model/
|       +-- Money.java               # 금액 Value Object
|       +-- Country.java             # 국가 Value Object
|
+-- entity/
|   +-- Payment.java                 # JPA 엔티티 (@Convert 사용, setter 유지)
|   +-- PaymentStatus.java           # 결제 상태 열거형
|
+-- converter/                       # JPA AttributeConverter
|   +-- MoneyConverter.java          # Money <-> Double 변환
|   +-- CountryConverter.java        # Country <-> String 변환
|
+-- dto/
|   +-- PaymentRequest.java          # 요청 DTO (외부 API용)
|   +-- PaymentResult.java           # 응답 DTO (외부 API용)
|
+-- policy/
|   +-- discount/
|   |   +-- CustomerDiscountPolicy.java  # 할인 정책 인터페이스
|   |   +-- VipDiscountPolicy.java       # VIP 할인 구현체
|   +-- tax/
|       +-- TaxPolicy.java               # 세금 정책 인터페이스
|       +-- KoreaVatPolicy.java          # 한국 VAT 구현체
|
+-- repository/
|   +-- PaymentRepository.java       # 저장소 인터페이스
|
+-- service/
|   +-- PaymentService.java          # 결제 서비스 (비즈니스 로직 포함)
|
+-- controller/
|   +-- PaymentController.java       # REST API
|
+-- handler/
    +-- PaymentCompletionHandler.java    # 완료 핸들러 인터페이스
    +-- PaymentAuditLogger.java          # 감사 로그 핸들러
    +-- SettlementRequestHandler.java    # 정산 요청 핸들러
```

---

## JPA에서 Value Object 매핑: @Convert vs @Embedded

JPA Entity에서 Value Object를 사용하려면 DB 컬럼과 매핑하는 방법을 선택해야함.

### 방법 1: @Convert (AttributeConverter)

**특징:**
- Value Object를 **단일 컬럼**에 저장
- Java 객체 <-> DB 값 양방향 변환
- 기존 DB 스키마 그대로 사용 가능

**적합한 경우:**
- Money (double 하나로 표현)
- Country (String 하나로 표현)
- 단일 값을 가진 Value Object

```java
// Converter 정의
@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, Double> {

    @Override
    public Double convertToDatabaseColumn(Money money) {
        // Java -> DB: Money 객체를 Double로 변환
        return money == null ? null : money.getAmount();
    }

    @Override
    public Money convertToEntityAttribute(Double amount) {
        // DB -> Java: Double을 Money 객체로 변환
        return amount == null ? null : Money.of(amount);
    }
}

// Entity에서 사용
@Entity
public class Payment {
    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false)
    private Money originalPrice;  // DB에는 DOUBLE 컬럼 하나
}
```

**DB 스키마:**
```sql
CREATE TABLE payments (
    original_price DOUBLE NOT NULL,  -- Money -> Double
    country VARCHAR(10) NOT NULL     -- Country -> String
);
```

### 방법 2: @Embedded (Embeddable)

**특징:**
- Value Object를 **여러 컬럼**에 저장
- Value Object의 필드가 그대로 테이블 컬럼이 됨

**적합한 경우:**
- Address (city, street, zipCode 여러 필드)
- 복합 값을 가진 Value Object

```java
// Value Object 정의
@Embeddable
public class Address {
    private String city;
    private String street;
    private String zipCode;
}

// Entity에서 사용
@Entity
public class Customer {
    @Embedded
    private Address address;  // DB에 city, street, zip_code 3개 컬럼
}
```

**DB 스키마:**
```sql
CREATE TABLE customers (
    city VARCHAR(100),
    street VARCHAR(200),
    zip_code VARCHAR(10)
);
```

### 이 프로젝트의 선택: @Convert

**선택 이유:**
1. Money와 Country 모두 **단일 값**으로 표현 가능
2. payment_ul과 **동일한 DB 스키마** 유지
3. 기존 데이터 **마이그레이션 불필요**
4. 구현이 단순함

**변환 흐름:**
```
[Java]                         [DB]
Money(10000.0)  ---저장--->    10000.0 (DOUBLE)
Money(10000.0)  <--조회---     10000.0 (DOUBLE)
                MoneyConverter

Country("KR")   ---저장--->    "KR" (VARCHAR)
Country("KR")   <--조회---     "KR" (VARCHAR)
                CountryConverter
```

---

## 주요 변경 내용

### 1. Service 계층 - Value Object 검증

**변경 전 (payment_ul) - 수동 검증 필요:**
```java
public PaymentResult processPayment(PaymentRequest request) {
    // 검증 로직이 Service에 있음
    if (request.originalPrice() < 0) {
        throw new IllegalArgumentException("가격은 0 이상이어야 합니다");
    }

    double discountedAmount = customerDiscountPolicy.apply(
            request.originalPrice(), request.isVip());
    double taxedAmount = taxPolicy.apply(discountedAmount);
    // ...
}
```

**변경 후 (payment_step1) - Value Object가 검증:**
```java
public PaymentResult processPayment(PaymentRequest request) {
    // Money.of()에서 음수 검증 -> 별도 if문 불필요
    // Country.of()에서 유효 국가 검증
    Money originalPrice = Money.of(request.originalPrice());
    Country country = Country.of(request.country());

    Money discountedAmount = customerDiscountPolicy.apply(originalPrice, request.isVip());
    Money taxedAmount = taxPolicy.apply(discountedAmount);
    // ...
}
```

### 2. Policy 인터페이스 - Money 타입 사용

**변경 전:**
```java
public interface CustomerDiscountPolicy {
    double apply(double originalPrice, boolean isVip);
}
```

**변경 후:**
```java
public interface CustomerDiscountPolicy {
    Money apply(Money originalPrice, boolean isVip);
}
```

### 3. Policy 구현체 - Money.multiply() 사용

**변경 전:**
```java
public double apply(double originalPrice, boolean isVip) {
    if (isVip) {
        return originalPrice * 0.85;
    }
    return originalPrice;
}
```

**변경 후:**
```java
public Money apply(Money originalPrice, boolean isVip) {
    if (isVip) {
        return originalPrice.multiply(0.85);  // Money의 연산 메서드 사용
    }
    return originalPrice;
}
```

### 4. Entity - Value Object + setter 유지

**변경 전 (payment_ul):**
```java
@Column(nullable = false)
private Double originalPrice;

@Column(nullable = false, length = 10)
private String country;

// setter로 상태 변경
public void setStatus(PaymentStatus status) { this.status = status; }
```

**변경 후 (payment_step1):**
```java
// Value Object 적용 + @Convert
@Convert(converter = MoneyConverter.class)
@Column(nullable = false)
private Money originalPrice;

@Convert(converter = CountryConverter.class)
@Column(nullable = false, length = 10)
private Country country;

// setter 유지 (payment_ul과 동일)
public void setStatus(PaymentStatus status) { this.status = status; }
```

---

## 변경되지 않은 점 (payment_ul과 동일)

### Entity는 Anemic Domain Model 유지

```java
// Service에서 상태 검증 및 변경
public Payment refundPayment(Long id) {
    Payment payment = getPayment(id);

    // 비즈니스 로직이 Service에 있음
    if (payment.getStatus() != PaymentStatus.COMPLETED) {
        throw new IllegalStateException("완료된 결제만 환불할 수 있습니다");
    }

    // setter로 상태 변경
    payment.setStatus(PaymentStatus.REFUNDED);
    payment.setUpdatedAt(LocalDateTime.now());

    return payment;
}
```

---

## Value Object 특징

### 1. 자가 검증 (Self-Validation)

```java
Money.of(-1000);  // IllegalArgumentException: 금액은 0 이상이어야 합니다
Country.of("JP"); // IllegalArgumentException: 지원하지 않는 국가입니다
```

### 2. 불변성 (Immutability)

```java
Money original = Money.of(10000);
Money discounted = original.multiply(0.9);  // 새 객체 반환

// original은 여전히 10000 (변경되지 않음)
// discounted는 9000
```

### 3. 값 동등성 (Value Equality)

```java
Money money1 = Money.of(10000);
Money money2 = Money.of(10000);

money1.equals(money2);  // true (같은 값이면 같은 객체)
```

---

## 실행 방법

```bash
./gradlew bootRun -PmainClass=com.example.payment_step1.PaymentStep1Application
```

---

## 테스트

```bash
# 전체 테스트
./gradlew test --tests "com.example.payment_step1.*"

# Value Object 테스트
./gradlew test --tests "com.example.payment_step1.domain.model.*"

# Service 단위 테스트
./gradlew test --tests "com.example.payment_step1.unit.*"

# Repository 테스트
./gradlew test --tests "com.example.payment_step1.repository.*"
```

---

## API 엔드포인트

| 메서드 | URL | 설명 |
|--------|-----|------|
| POST | /api/step1/payments | 결제 생성 |
| GET | /api/step1/payments/{id} | 결제 조회 |
| GET | /api/step1/payments | 전체 조회 |
| PATCH | /api/step1/payments/{id}/refund | 환불 |

---

## 요청/응답 예시

### 결제 생성 요청

```json
POST /api/step1/payments
{
  "originalPrice": 10000,
  "country": "KR",
  "isVip": true
}
```

### 응답

```json
{
  "originalPrice": 10000.0,
  "discountedAmount": 8500.0,
  "taxedAmount": 9350.0,
  "country": "KR",
  "isVip": true
}
```

---

## 변경 효과 요약

| 효과 | 설명 |
|------|------|
| 자동 검증 | Money.of(), Country.of()에서 유효성 검사 |
| 타입 안전성 | Money와 Country를 혼동할 수 없음 |
| 로직 캡슐화 | money.multiply(0.9) 같은 연산이 객체 안에 |
| 코드 간결화 | Service에서 if문 검증 제거 |
| DB 호환성 | @Convert로 기존 스키마 그대로 사용 |

---

## 다음 단계
- Rich Domain Model (비즈니스 메서드를 Entity에 캡슐화).
  - Entity에서 complete(), refund() 같은 비즈니스 메서드 제공
  - setter 제거
  - 상태 전이 규칙을 Entity 안에 캡슐화
