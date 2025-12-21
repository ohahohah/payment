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
9. [테스트 코드 구조](#테스트-코드-구조)
10. [API 사용 방법](#api-사용-방법)
11. [실행 방법](#실행-방법)

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
│   │   └── PaymentConfig.java           # 스프링 설정 (외부 라이브러리 빈용)
│   │
│   ├── controller/
│   │   └── PaymentController.java       # REST API 컨트롤러
│   │
│   ├── service/
│   │   └── PaymentService.java          # 모든 비즈니스 로직 처리
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
│   │   │   └── DefaultDiscountPolicy.java # @Component @Primary
│   │   │
│   │   └── tax/
│   │       ├── TaxPolicy.java           # 세금 전략 인터페이스
│   │       ├── KoreaTaxPolicy.java      # @Component @Primary
│   │       └── UsTaxPolicy.java         # @Component
│   │
│   └── listener/
│       ├── PaymentListener.java         # 옵저버 인터페이스
│       ├── LoggingListener.java         # 로깅 옵저버 (@Component)
│       └── SettlementListener.java      # 정산 옵저버 (@Component)
│
├── src/main/resources/
│   └── application.yml                  # 애플리케이션 설정 (DB, JPA 포함)
│
└── build.gradle                         # Gradle 빌드 설정
```

---

## 아키텍처 다이어그램

### 레이어 구조

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
│   ┌─────────────────────────────────────────────────────┐   │
│   │                   PaymentService                    │   │
│   │  - @Transactional (트랜잭션 관리)                    │   │
│   │  - 할인/세금 계산 (비즈니스 로직)                    │   │
│   │  - 상태 변경 (도메인 로직)                           │   │
│   │  - 리스너 알림                                       │   │
│   └─────────────────────────────────────────────────────┘   │
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
3. PaymentService (모든 비즈니스 로직)
   ┌─────────────────────────────────────┐
   │ 할인 정책 적용 (DiscountPolicy)      │
   │ 세금 정책 적용 (TaxPolicy)           │
   │ 엔티티 생성 및 상태 변경             │
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
│   │ - Getter/Setter만 제공 (데이터 홀더)                         │   │
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
        // 1. 할인/세금 계산 (비즈니스 로직)
        double discounted = discountPolicy.apply(request.originalPrice(), request.isVip());
        double taxed = taxPolicy.apply(discounted);

        // 2. 엔티티 생성 및 저장
        Payment payment = Payment.create(...);
        paymentRepository.save(payment);  // INSERT

        // 3. 상태 변경 (Dirty Checking으로 자동 UPDATE)
        payment.setStatus(PaymentStatus.COMPLETED);

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
// 스프링이 List<PaymentListener>를 자동으로 수집하여 주입
for (PaymentListener listener : listeners) {
    listener.onPaymentCompleted(result);
}
```

---

## 스프링 핵심 개념

### 주요 어노테이션

| 어노테이션 | 용도 |
|-----------|------|
| `@SpringBootApplication` | 애플리케이션 진입점, 자동 설정 활성화 |
| `@Component` | 일반 컴포넌트 빈 자동 등록 (Spring Boot 권장) |
| `@Service` | 비즈니스 로직 계층 빈 자동 등록 |
| `@Repository` | 데이터 접근 계층 빈 자동 등록 |
| `@RestController` | REST API 컨트롤러 |
| `@Configuration` / `@Bean` | 외부 라이브러리 빈 수동 등록 시에만 사용 |
| `@Primary` | 동일 타입 빈이 여러 개일 때 기본 빈 지정 |
| `@Entity` / `@Table` | JPA 엔티티 / 테이블 매핑 |
| `@Id` / `@GeneratedValue` | 기본 키 / 자동 생성 전략 |
| `@Column` / `@Enumerated` | 컬럼 설정 / Enum 저장 방식 |
| `@Transactional` | 트랜잭션 경계 설정 |
| `@Query` | 직접 JPQL/SQL 작성 |

### Spring Boot 빈 등록 방식

**1. @Component 스캔 (권장)**
```java
// 클래스에 직접 어노테이션을 붙이면 자동으로 빈 등록
@Component
@Primary  // 같은 타입의 빈이 여러 개일 때 기본 선택
public class DefaultDiscountPolicy implements DiscountPolicy { ... }

@Component
public class LoggingListener implements PaymentListener { ... }
```

**장점:**
- 해당 클래스 파일만 보면 빈인지 알 수 있음 (가독성)
- Config 클래스가 비대해지지 않음 (유지보수성)
- 빈 등록과 클래스 정의가 분리되지 않음 (응집도)

**2. @Bean 수동 등록 (외부 라이브러리용)**
```java
@Configuration
public class PaymentConfig {
    // 외부 라이브러리 클래스는 @Component를 붙일 수 없으므로 @Bean 사용
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule());
    }
}
```

**3. List<T> 자동 수집**
```java
@Service
public class PaymentProcessor {
    // 스프링이 PaymentListener 타입의 모든 빈을 자동으로 수집하여 주입
    public PaymentProcessor(List<PaymentListener> listeners) {
        this.listeners = listeners;  // LoggingListener, SettlementListener 자동 수집
    }
}
```

---

## 테스트 코드 구조

### 테스트 패키지 구조

```
src/test/java/com/example/payment/
│
├── todoFix/                              # [리팩토링 대상] 엉망진창인 테스트
│   └── MessyPaymentTest.java             # 문제점이 많은 테스트 코드
│
├── unit/                                 # [권장] 단위 테스트 (스프링 컨텍스트 없음)
│   ├── entity/
│   │   └── PaymentEntityTest.java        # 엔티티 도메인 로직 테스트
│   ├── policy/
│   │   ├── DiscountPolicyTest.java       # 할인 정책 POJO 테스트
│   │   └── TaxPolicyTest.java            # 세금 정책 POJO 테스트
│   └── service/
│       └── PaymentProcessorTest.java     # Mockito 기반 단위 테스트
│
├── integration/                          # [권장] 통합 테스트
│   ├── repository/
│   │   └── PaymentRepositoryTest.java    # @DataJpaTest 슬라이스
│   └── service/
│       └── PaymentServiceIntegrationTest.java  # @SpringBootTest 통합
│
└── web/                                  # [권장] 웹 레이어 테스트
    └── PaymentControllerTest.java        # @WebMvcTest 슬라이스
```

### 테스트 어노테이션 비교

| 테스트 유형 | 어노테이션 | 로딩 범위 | 속도 | 사용 시점 |
|------------|-----------|----------|------|----------|
| 단위 테스트 | 없음 / `@ExtendWith(MockitoExtension.class)` | 스프링 컨텍스트 없음 | 가장 빠름 | POJO 로직 검증 |
| Repository 슬라이스 | `@DataJpaTest` | JPA 관련만 | 빠름 | Repository 쿼리 검증 |
| Controller 슬라이스 | `@WebMvcTest` | MVC 관련만 | 빠름 | HTTP 요청/응답 검증 |
| 전체 통합 테스트 | `@SpringBootTest` | 전체 컨텍스트 | 느림 | E2E 플로우 검증 |

### 테스트 피라미드

```
        /\          E2E / 통합 테스트 (적게)
       /  \         - @SpringBootTest
      /----\
     /      \       슬라이스 테스트 (적당히)
    /--------\      - @WebMvcTest, @DataJpaTest
   /          \
  /------------\    단위 테스트 (많이)
 /              \   - 순수 POJO, Mockito
```

### todoFix vs 정리된 테스트 비교

#### todoFix/MessyPaymentTest.java의 문제점

```java
// [문제점 1] POJO 테스트에 @SpringBootTest 사용 - 불필요하게 무거움!
@SpringBootTest
@AutoConfigureMockMvc
class MessyPaymentTest {

    // [문제점 2] POJO 테스트인데 스프링 의존성
    @Autowired
    private MockMvc mockMvc;

    // [문제점 3] 매직 넘버 사용 - 의미 파악 어려움
    @Test
    void test1() {
        double result = new DefaultDiscountPolicy().apply(10000, true);
        assertEquals(8500, result);  // 8500이 뭐지?
    }

    // [문제점 4] 테스트 메서드명이 불명확
    @Test
    void testPayment() { ... }

    // [문제점 5] @Nested로 그룹화 안 됨
    // [문제점 6] Given-When-Then 구조 없음
    // [문제점 7] 테스트 간 데이터 격리 미흡
}
```

#### 정리된 테스트 코드 예시

**1. 단위 테스트 (unit/policy/DiscountPolicyTest.java)**

```java
// 스프링 컨텍스트 없이 순수 Java 테스트
@DisplayName("할인 정책 단위 테스트")
class DiscountPolicyTest {

    private static final double VIP_DISCOUNT_RATE = 0.15;  // 상수로 의미 명확화

    @Nested
    @DisplayName("VIP 고객 할인 테스트")  // 논리적 그룹화
    class VipDiscountTest {

        @ParameterizedTest(name = "원가 {0}원 → VIP 할인가 {1}원")
        @CsvSource({"10000, 8500", "20000, 17000"})
        void shouldCalculateCorrectVipDiscount(double original, double expected) {
            // Given
            DiscountPolicy policy = new DefaultDiscountPolicy();

            // When
            double discounted = policy.apply(original, true);

            // Then
            assertThat(discounted)
                .as("VIP 15%% 할인 적용")
                .isEqualTo(expected);
        }
    }
}
```

**2. 엔티티 단위 테스트 (unit/entity/PaymentEntityTest.java)**

```java
// 스프링 없이 순수 POJO 테스트
@DisplayName("Payment 엔티티 단위 테스트")
class PaymentEntityTest {

    @Nested
    @DisplayName("결제 생성 테스트")
    class PaymentCreationTest {

        @Test
        @DisplayName("생성된 결제의 초기 상태는 PENDING이다")
        void newPaymentShouldHavePendingStatus() {
            // When
            Payment payment = Payment.create(10000.0, 8500.0, 9350.0, "KR", true);

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Setter 테스트")
    class SetterTest {

        @Test
        @DisplayName("setStatus로 상태를 변경할 수 있다")
        void shouldChangeStatusWithSetter() {
            // Given
            Payment payment = Payment.create(10000.0, 8500.0, 9350.0, "KR", true);

            // When
            payment.setStatus(PaymentStatus.COMPLETED);

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }
    }
}
```

**3. Repository 슬라이스 테스트 (integration/repository/PaymentRepositoryTest.java)**

```java
@DataJpaTest  // JPA 관련 빈만 로딩 (가벼움)
@DisplayName("PaymentRepository 슬라이스 테스트")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TestEntityManager entityManager;  // 테스트용 EntityManager

    @Test
    @DisplayName("상태별로 결제를 조회할 수 있다")
    void shouldFindByStatus() {
        // Given - TestEntityManager로 직접 저장
        entityManager.persistAndFlush(createCompletedPayment());
        entityManager.clear();  // 1차 캐시 초기화

        // When
        List<Payment> payments = paymentRepository.findByStatus(COMPLETED);

        // Then
        assertThat(payments).hasSize(1);
    }
}
```

**4. Controller 슬라이스 테스트 (web/PaymentControllerTest.java)**

```java
@WebMvcTest(PaymentController.class)  // Controller만 테스트
@DisplayName("PaymentController 웹 레이어 테스트")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;  // HTTP 요청 시뮬레이션

    @MockBean  // 스프링 컨텍스트에 Mock 빈 등록
    private PaymentService paymentService;

    @Test
    @DisplayName("유효한 요청으로 결제 생성 시 200 OK 반환")
    void shouldCreatePayment() throws Exception {
        // Given
        given(paymentService.processPayment(any()))
            .willReturn(new PaymentResult(...));

        // When & Then
        mockMvc.perform(post("/api/payments")
                .contentType(APPLICATION_JSON)
                .content("{\"originalPrice\": 10000}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.taxedAmount").value(9350));
    }
}
```

**5. 전체 통합 테스트 (integration/service/PaymentServiceIntegrationTest.java)**

```java
@SpringBootTest      // 전체 컨텍스트 로딩
@Transactional       // 테스트 후 자동 롤백
@DisplayName("PaymentService 통합 테스트")
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("결제 생성 → 조회 → 환불 전체 플로우")
    void completePaymentFlow() {
        // 1. 결제 생성
        PaymentResult result = paymentService.processPayment(
            new PaymentRequest(50000, "KR", true)
        );
        assertThat(result.taxedAmount()).isEqualTo(46750);

        // 2. 조회
        Payment payment = paymentRepository.findAll().get(0);
        assertThat(payment.getStatus()).isEqualTo(COMPLETED);

        // 3. 환불
        Payment refunded = paymentService.refundPayment(payment.getId());
        assertThat(refunded.getStatus()).isEqualTo(REFUNDED);
    }
}
```

### 테스트 코드 개선 체크리스트

| 항목 | todoFix | 정리된 테스트 |
|------|---------|--------------|
| 적절한 테스트 어노테이션 |  모든 곳에 @SpringBootTest | (권장) 레이어별 적절한 어노테이션 |
| 테스트 메서드명 |  test1, testPayment | (권장) @DisplayName으로 한글 설명 |
| 논리적 그룹화 |  평면적 구조 | (권장) @Nested로 계층화 |
| 매직 넘버 |  하드코딩된 숫자 | (권장) 상수로 의미 명확화 |
| Given-When-Then |  구조 없음 | (권장) 명확한 구조 |
| Assertion 메시지 |  없음 | (권장) as()로 실패 시 힌트 제공 |
| 다양한 케이스 |  단일 케이스 | (권장) @ParameterizedTest |
| 데이터 격리 |  테스트 간 영향 | (권장) @Transactional 롤백 |

### 테스트 실행 방법

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스만 실행
./gradlew test --tests "PaymentProcessorTest"

# 특정 패키지만 실행
./gradlew test --tests "com.example.payment.unit.*"

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

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
| Service | PaymentService | 트랜잭션 관리, 비즈니스 로직, 상태 변경 |
| Repository | PaymentRepository | 데이터베이스 접근 |
| Entity | Payment | DB 테이블 매핑 (데이터 홀더) |

### SOLID 원칙 적용

| 원칙 | 적용 |
|------|------|
| SRP (단일 책임) | Service/Repository 역할 분리 |
| OCP (개방-폐쇄) | 새 정책 추가 시 기존 코드 수정 불필요 |
| DIP (의존성 역전) | 인터페이스(DiscountPolicy)에 의존 |
