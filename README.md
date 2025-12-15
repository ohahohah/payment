# Payment Processor

스프링 부트 기반의 결제 처리 시스템 예제 프로젝트

---

## 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [기술 스택](#기술-스택)
3. [프로젝트 구조](#프로젝트-구조)
4. [아키텍처 다이어그램](#아키텍처-다이어그램)
5. [핵심 개념 설명](#핵심-개념-설명)
6. [JPA와 데이터베이스](#jpa와-데이터베이스)
7. [디자인 패턴](#디자인-패턴)
8. [스프링 핵심 개념](#스프링-핵심-개념)
9. [API 사용 방법](#api-사용-방법)
10. [실행 방법](#실행-방법)

---

## 프로젝트 개요

이 프로젝트는 결제 금액을 계산하고 저장하는 시스템입니다.

### 비즈니스 플로우

```
원래 가격 → [할인 정책 적용] → [세금 정책 적용] → [DB 저장] → 최종 가격
```

### 주요 기능

- **할인 적용**: VIP 고객 15% 할인, 일반 고객 10% 할인
- **세금 적용**: 한국 10% VAT, 미국 7% Sales Tax
- **DB 저장**: H2 데이터베이스에 결제 내역 저장
- **이벤트 알림**: 결제 완료 시 로깅 및 정산 요청
- **환불 처리**: 완료된 결제 환불 기능

---

## 기술 스택

| 기술 | 버전 | 설명 |
|------|------|------|
| Java | 17 | LTS 버전, Record, Switch Expression 사용 |
| Spring Boot | 3.2.0 | 웹 애플리케이션 프레임워크 |
| Spring Data JPA | 3.2.0 | ORM 프레임워크 |
| H2 Database | 2.x | 인메모리 데이터베이스 (개발/테스트용) |
| Gradle | 8.x | 빌드 도구 |

---

## 프로젝트 구조

```
payment-processor/
├── src/main/java/com/example/payment/
│   ├── PaymentApplication.java          # 애플리케이션 진입점
│   │
│   ├── config/
│   │   └── PaymentConfig.java           # 스프링 빈 설정
│   │
│   ├── controller/
│   │   └── PaymentController.java       # REST API 컨트롤러
│   │
│   ├── service/
│   │   ├── PaymentService.java          # 트랜잭션 관리 + 비즈니스 흐름
│   │   └── PaymentProcessor.java        # 핵심 비즈니스 로직 (금액 계산)
│   │
│   ├── repository/
│   │   └── PaymentRepository.java       # 데이터 접근 계층 (JPA Repository)
│   │
│   ├── entity/
│   │   ├── Payment.java                 # 결제 엔티티 (JPA Entity)
│   │   └── PaymentStatus.java           # 결제 상태 Enum
│   │
│   ├── dto/
│   │   ├── PaymentRequest.java          # 요청 DTO (Record)
│   │   ├── PaymentResult.java           # 처리 결과 DTO (Record)
│   │   └── PaymentResponse.java         # 응답 DTO (Record)
│   │
│   ├── policy/
│   │   ├── discount/
│   │   │   ├── DiscountPolicy.java      # 할인 전략 인터페이스
│   │   │   └── DefaultDiscountPolicy.java # 기본 할인 구현체
│   │   │
│   │   └── tax/
│   │       ├── TaxPolicy.java           # 세금 전략 인터페이스
│   │       ├── KoreaTaxPolicy.java      # 한국 세금 구현체
│   │       └── UsTaxPolicy.java         # 미국 세금 구현체
│   │
│   ├── listener/
│   │   ├── PaymentListener.java         # 옵저버 인터페이스
│   │   ├── LoggingListener.java         # 로깅 옵저버
│   │   └── SettlementListener.java      # 정산 옵저버
│   │
│   └── factory/
│       ├── PaymentProcessorFactory.java      # 팩토리 추상 클래스
│       └── DefaultPaymentProcessorFactory.java # 팩토리 구현체
│
├── src/main/resources/
│   └── application.yml                  # 애플리케이션 설정 (DB, JPA 포함)
│
└── build.gradle                         # Gradle 빌드 설정
```

---

## 아키텍처 다이어그램

### 레이어 구조 (현업 구조)

```
┌─────────────────────────────────────────────────────────────┐
│                     Controller Layer                        │
│   ┌─────────────────────────────────────────────────────┐   │
│   │              PaymentController                      │   │
│   │         (REST API 요청/응답 처리)                    │   │
│   └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Service Layer                          │
│   ┌──────────────────────┐   ┌──────────────────────┐       │
│   │   PaymentService     │──▶│  PaymentProcessor    │       │
│   │ (@Transactional)     │   │   (비즈니스 로직)     │       │
│   └──────────────────────┘   └──────────────────────┘       │
└─────────────────────────────────────────────────────────────┘
                    │              │
           ┌───────┘              └───────┐
           ▼                              ▼
┌──────────────────────┐      ┌──────────────────────┐
│  Repository Layer    │      │   Policy Layer       │
│                      │      │  (전략 패턴)          │
│  PaymentRepository   │      │                      │
│  (Spring Data JPA)   │      │  DiscountPolicy      │
│                      │      │  TaxPolicy           │
└──────────────────────┘      └──────────────────────┘
           │
           ▼
┌──────────────────────┐
│    Database Layer    │
│                      │
│   H2 (In-Memory)     │
│   테이블: payments   │
└──────────────────────┘
```

### 데이터 흐름

```
1. HTTP 요청
   POST /api/payments
   ┌────────────────────┐
   │ PaymentRequest     │
   │ - originalPrice    │
   │ - country          │
   │ - isVip            │
   └────────────────────┘
            │
            ▼
2. Controller → Service (트랜잭션 시작)
            │
            ▼
3. PaymentProcessor (비즈니스 로직)
   ┌─────────────────────────────────────┐
   │ 할인 정책 적용 (DiscountPolicy)      │
   │ 세금 정책 적용 (TaxPolicy)           │
   │ 리스너 알림 (PaymentListener)        │
   └─────────────────────────────────────┘
            │
            ▼
4. Repository → Database (저장)
   ┌────────────────────┐
   │ Payment Entity     │
   │ - id               │
   │ - originalPrice    │
   │ - discountedAmount │
   │ - taxedAmount      │
   │ - status           │
   │ - createdAt        │
   └────────────────────┘
            │
            ▼
5. 트랜잭션 커밋 → HTTP 응답
   ┌────────────────────┐
   │ PaymentResponse    │
   └────────────────────┘
```

---

## 핵심 개념 설명

### DTO vs Entity 구분

```
┌─────────────────────────────────────────────────────────────────────┐
│                          DTO (Data Transfer Object)                 │
│   ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐   │
│   │ PaymentRequest  │   │ PaymentResult   │   │ PaymentResponse │   │
│   │ (요청 전용)      │   │ (처리 결과)     │   │ (응답 전용)     │   │
│   └─────────────────┘   └─────────────────┘   └─────────────────┘   │
│                                                                     │
│   - Controller와 외부 통신에 사용                                    │
│   - 비즈니스 로직 없음                                               │
│   - 불변 객체 (Record 사용)                                          │
└─────────────────────────────────────────────────────────────────────┘
                              │
                     DTO ↔ Entity 변환
                              │
┌─────────────────────────────────────────────────────────────────────┐
│                         Entity (JPA 엔티티)                          │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                         Payment                             │   │
│   │ - DB 테이블과 1:1 매핑                                       │   │
│   │ - 도메인 로직 포함 (complete(), refund() 등)                  │   │
│   │ - JPA가 관리하는 영속 객체                                    │   │
│   └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## JPA와 데이터베이스

### 1. JPA 엔티티 (Payment.java)

```java
@Entity                              // JPA 엔티티임을 선언
@Table(name = "payments")            // 매핑할 테이블 지정
public class Payment {

    @Id                              // 기본 키(PK)
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 자동 증가
    private Long id;

    @Column(nullable = false)        // NOT NULL 제약조건
    private Double originalPrice;

    @Enumerated(EnumType.STRING)     // Enum을 문자열로 저장
    private PaymentStatus status;

    @Column(updatable = false)       // 수정 불가 (생성 시에만 설정)
    private LocalDateTime createdAt;
}
```

### 2. Spring Data JPA Repository

```java
// 인터페이스만 정의하면 구현체 자동 생성!
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 쿼리 메서드 - 메서드 이름으로 쿼리 자동 생성
    List<Payment> findByStatus(PaymentStatus status);
    // → SELECT * FROM payments WHERE status = ?

    List<Payment> findByCountryAndStatus(String country, PaymentStatus status);
    // → SELECT * FROM payments WHERE country = ? AND status = ?

    // JPQL 직접 작성
    @Query("SELECT SUM(p.taxedAmount) FROM Payment p WHERE p.status = :status")
    Double sumTaxedAmountByStatus(@Param("status") PaymentStatus status);

    // 네이티브 SQL
    @Query(value = "SELECT * FROM payments ORDER BY created_at DESC LIMIT :limit",
           nativeQuery = true)
    List<Payment> findRecentPayments(@Param("limit") int limit);
}
```

### 3. 트랜잭션 관리 (@Transactional)

```java
@Service
public class PaymentService {

    @Transactional  // 이 메서드를 트랜잭션으로 감싸기
    public PaymentResult processPayment(PaymentRequest request) {
        // 1. 비즈니스 로직 수행
        PaymentResult result = paymentProcessor.process(...);

        // 2. 엔티티 생성 및 저장
        Payment payment = Payment.create(...);
        paymentRepository.save(payment);  // INSERT

        // 3. 상태 변경 (Dirty Checking으로 자동 UPDATE)
        payment.complete();

        // 메서드 정상 종료 시 자동 COMMIT
        // 예외 발생 시 자동 ROLLBACK
        return result;
    }

    @Transactional(readOnly = true)  // 읽기 전용 (성능 최적화)
    public Payment getPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다"));
    }
}
```

### 4. H2 콘솔 접속

애플리케이션 실행 후 브라우저에서 접속:
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:paymentdb
- User: sa
- Password: (비워두기)

---

## 디자인 패턴

### 1. 전략 패턴 (Strategy Pattern)

```java
// 인터페이스
public interface DiscountPolicy {
    double apply(double originalPrice, boolean isVip);
}

// 구현체
public class DefaultDiscountPolicy implements DiscountPolicy {
    @Override
    public double apply(double originalPrice, boolean isVip) {
        return isVip ? originalPrice * 0.85 : originalPrice * 0.90;
    }
}
```

### 2. 옵저버 패턴 (Observer Pattern)

```java
// Observer 인터페이스
public interface PaymentListener {
    void onPaymentCompleted(PaymentResult result);
}

// Subject가 모든 Observer에게 알림
for (PaymentListener listener : listeners) {
    listener.onPaymentCompleted(result);
}
```

### 3. 팩토리 메서드 패턴 (Factory Method Pattern)

```java
public abstract class PaymentProcessorFactory {
    public PaymentProcessor create(String country, List<PaymentListener> listeners) {
        DiscountPolicy discount = createDiscountPolicy();    // 팩토리 메서드
        TaxPolicy tax = createTaxPolicy(country);            // 팩토리 메서드
        return new PaymentProcessor(discount, tax, listeners);
    }

    protected abstract DiscountPolicy createDiscountPolicy();
    protected abstract TaxPolicy createTaxPolicy(String country);
}
```

---

## 스프링 핵심 개념

### 주요 어노테이션

| 어노테이션 | 용도 |
|-----------|------|
| `@SpringBootApplication` | 애플리케이션 진입점, 자동 설정 활성화 |
| `@Configuration` / `@Bean` | 빈 설정 클래스 / 빈 등록 메서드 |
| `@Component` / `@Service` / `@Repository` | 컴포넌트 스캔으로 빈 등록 |
| `@RestController` | REST API 컨트롤러 |
| `@Entity` / `@Table` | JPA 엔티티 / 테이블 매핑 |
| `@Id` / `@GeneratedValue` | 기본 키 / 자동 생성 전략 |
| `@Column` / `@Enumerated` | 컬럼 설정 / Enum 저장 방식 |
| `@Transactional` | 트랜잭션 경계 설정 |
| `@Query` | 직접 JPQL/SQL 작성 |

---

## API 사용 방법

### 결제 생성
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{"originalPrice": 10000, "country": "KR", "isVip": true}'
```

### 결제 조회 (단건)
```bash
curl http://localhost:8080/api/payments/1
```

### 결제 목록 조회
```bash
curl http://localhost:8080/api/payments
```

### 상태별 조회
```bash
curl "http://localhost:8080/api/payments/status?status=COMPLETED"
```

### 최근 결제 조회
```bash
curl "http://localhost:8080/api/payments/recent?limit=5"
```

### 환불 처리
```bash
curl -X PATCH http://localhost:8080/api/payments/1/refund
```

---

## 실행 방법

### 1. 빌드
```bash
./gradlew build
```

### 2. 실행
```bash
./gradlew bootRun
```

### 3. H2 콘솔 접속
- http://localhost:8080/h2-console

### 4. API 테스트
```bash
# 결제 생성
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{"originalPrice": 10000, "country": "KR", "isVip": true}'

# 결제 조회
curl http://localhost:8080/api/payments

# 환불
curl -X PATCH http://localhost:8080/api/payments/1/refund
```

---

## 학습 포인트 요약

### 레이어별 역할

| 레이어 | 클래스 | 역할 |
|--------|--------|------|
| Controller | PaymentController | HTTP 요청/응답 처리 |
| Service | PaymentService | 트랜잭션 관리, 비즈니스 흐름 조율 |
| Domain | PaymentProcessor | 순수 비즈니스 로직 (금액 계산) |
| Repository | PaymentRepository | 데이터베이스 접근 |
| Entity | Payment | DB 테이블 매핑 + 도메인 로직 |

### SOLID 원칙 적용

| 원칙 | 적용 |
|------|------|
| SRP (단일 책임) | Service/Processor/Repository 역할 분리 |
| OCP (개방-폐쇄) | 새 정책 추가 시 기존 코드 수정 불필요 |
| DIP (의존성 역전) | 인터페이스(DiscountPolicy)에 의존 |
