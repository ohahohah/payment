# 테스트 리팩토링 가이드

## 개요

`todoFix/MessyPaymentTest.java`에서 발견되는 문제점들과 이를 개선한 테스트 구조를 비교합니다.

---

## 1. 문제점: MessyPaymentTest (todoFix)

### 1-1. 불필요한 Spring Context 로딩

```java
@SpringBootTest  // POJO 테스트에 전체 Spring Context 로딩
class MessyPaymentTest {
    @Test
    void test1() {
        DiscountStrategy strategy = new DefaultDiscountStrategy();
        // 순수 Java 객체 테스트인데 Spring Boot를 띄움
    }
}
```

**문제점:**
- 단순 POJO 테스트에 `@SpringBootTest` 사용
- 테스트 실행 시간 불필요하게 증가 (수 초 → 수십 밀리초로 단축 가능)
- 테스트 격리 원칙 위반

### 1-2. 매직 넘버 사용

```java
// BAD: 숫자의 의미를 알 수 없음
assertEquals(90.0, result);  // 90.0이 뭘 의미하는지?
assertEquals(99.0, result);  // 왜 99.0인지?
```

### 1-3. 모든 테스트가 한 파일에 혼재

```java
class MessyPaymentTest {
    void test1() { /* 할인 정책 테스트 */ }
    void test2() { /* 세금 정책 테스트 */ }
    void test3() { /* 엔티티 테스트 */ }
    void test4() { /* Repository 테스트 */ }
    // 관심사 분리 없이 모든 테스트가 한 곳에
}
```

### 1-4. 의미 없는 테스트명

```java
void test1() { }  // 무엇을 테스트하는지 알 수 없음
void test2() { }
void test3() { }
```

---

## 2. 개선된 테스트 구조

```
src/test/java/com/example/payment/
├── unit/                    # 순수 단위 테스트 (Spring 없음)
│   ├── policy/
│   │   ├── DiscountPolicyTest.java
│   │   └── TaxPolicyTest.java
│   ├── entity/
│   │   └── PaymentEntityTest.java
│   └── service/
│       └── PaymentServiceTest.java
├── integration/             # 슬라이스 테스트
│   └── repository/
│       └── PaymentRepositoryTest.java
└── web/                     # 웹 계층 테스트
    └── PaymentControllerTest.java
```

---

## 3. 주요 개선 포인트

### 3-1. 적절한 테스트 애노테이션 선택

| 테스트 유형 | 애노테이션 | 용도 |
|------------|-----------|------|
| 순수 단위 테스트 | 없음 (Plain JUnit) | POJO, 비즈니스 로직 |
| Repository 테스트 | `@DataJpaTest` | JPA 슬라이스만 로딩 |
| Controller 테스트 | `@WebMvcTest` | 웹 레이어만 로딩 |
| 통합 테스트 | `@SpringBootTest` | 전체 플로우 검증 (최소화) |

```java
// GOOD: POJO 테스트에는 Spring 애노테이션 없음
class DiscountPolicyTest {
    @Test
    void vip_customer_gets_10_percent_discount() {
        DiscountStrategy strategy = new DefaultDiscountStrategy();
        // ...
    }
}

// GOOD: Repository는 @DataJpaTest로 JPA만 로딩
@DataJpaTest
class PaymentRepositoryTest {
    @Autowired
    private TestEntityManager em;
}
```

### 3-2. 상수로 의미 부여

```java
// GOOD: 상수로 테스트 의도 명확화
class DiscountPolicyTest {
    private static final double VIP_DISCOUNT_RATE = 0.10;
    private static final double ORIGINAL_PRICE = 100.0;
    private static final double EXPECTED_VIP_PRICE = 90.0;  // 100 * (1 - 0.10)

    @Test
    void vip_customer_gets_10_percent_discount() {
        double result = strategy.apply(ORIGINAL_PRICE, IS_VIP);
        assertThat(result).isEqualTo(EXPECTED_VIP_PRICE);
    }
}
```

### 3-3. @Nested로 논리적 그룹화

```java
class TaxPolicyTest {

    @Nested
    @DisplayName("한국 세금 정책")
    class KoreaTaxPolicyTest {
        @Test void 부가세_10퍼센트_적용() { }
    }

    @Nested
    @DisplayName("미국 세금 정책")
    class UsTaxPolicyTest {
        @Test void 판매세_7퍼센트_적용() { }
    }
}
```

**장점:**
- 관련 테스트를 논리적으로 그룹화
- 테스트 리포트에서 계층 구조로 표시
- 각 그룹별 `@BeforeEach` 설정 가능

### 3-4. @ParameterizedTest로 다양한 케이스 커버

```java
@ParameterizedTest
@CsvSource({
    "100.0, true, 90.0",   // VIP: 10% 할인
    "100.0, false, 100.0", // 일반: 할인 없음
    "0.0, true, 0.0",      // 경계값: 0원
    "1000.0, true, 900.0"  // 큰 금액
})
void discount_applied_correctly(double price, boolean isVip, double expected) {
    assertThat(strategy.apply(price, isVip)).isEqualTo(expected);
}
```

**장점:**
- 반복적인 테스트 코드 제거
- 다양한 입력값 테스트
- 경계값 테스트 용이

### 3-5. Given-When-Then 구조

```java
@Test
void completed_payment_can_be_refunded() {
    // Given: 완료된 결제가 있을 때
    Payment payment = Payment.create(100.0, 90.0, 99.0, "KR", true);
    payment.setStatus(PaymentStatus.COMPLETED);
    Payment saved = repository.save(payment);

    // When: 환불 상태로 변경하면
    saved.setStatus(PaymentStatus.REFUNDED);
    Payment updated = repository.save(saved);

    // Then: 상태가 REFUNDED로 변경됨
    assertThat(updated.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
}
```

### 3-6. 슬라이스 테스트 활용

```java
@DataJpaTest  // JPA 관련 빈만 로딩 (전체 Context X)
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager em;  // 테스트용 EntityManager

    @Autowired
    private PaymentRepository repository;

    @Test
    void save_and_find_payment() {
        // TestEntityManager로 직접 영속화
        Payment payment = em.persistAndFlush(Payment.create(...));

        // Repository로 조회
        Optional<Payment> found = repository.findById(payment.getId());

        assertThat(found).isPresent();
    }
}
```

### 3-7. Controller 테스트는 @WebMvcTest 사용

```java
// BAD: Controller 테스트에 전체 Context 로딩
@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest { }

// GOOD: 웹 레이어만 로딩
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean  // Service는 Mock으로 대체
    private PaymentService paymentService;
}
```

**@WebMvcTest vs @SpringBootTest:**
- `@WebMvcTest`: Controller, Filter, ControllerAdvice만 로딩 (빠름)
- `@SpringBootTest`: 전체 Application Context 로딩 (느림)
- Service를 `@MockBean`으로 대체하면 `@WebMvcTest`로 충분

### 3-8. Service 테스트는 단위 테스트로

```java
// BAD: Service 테스트에 @SpringBootTest 사용
@SpringBootTest
class PaymentServiceIntegrationTest {
    @Autowired
    private PaymentService paymentService;
    // 전체 Context 로딩 - 느림
}

// GOOD: Mockito로 단위 테스트
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock PaymentRepository repository;
    @Mock DiscountStrategy discountStrategy;
    @Mock TaxStrategy taxStrategy;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(repository, discountStrategy, ...);
    }

    @Test
    void shouldApplyDiscountAndTax() {
        given(discountStrategy.apply(10000, true)).willReturn(8500.0);
        given(taxStrategy.apply(8500.0)).willReturn(9350.0);

        // Service 로직만 검증
    }
}
```

**Service 단위 테스트에서 검증할 것:**
- 의존성(Repository, Strategy)이 올바르게 호출되는가
- 비즈니스 로직 흐름이 맞는가
- 예외 상황이 올바르게 처리되는가

**@SpringBootTest가 필요한 경우:**
- E2E 테스트 (최소화)
- 여러 서비스 간 연동 테스트

---

## 4. 테스트 피라미드

```
        /\
       /  \        E2E (느림, 비쌈)
      /----\
     /      \      Integration (@DataJpaTest, @WebMvcTest)
    /--------\
   /          \    Unit Tests (빠름, 저렴)
  /------------\
```

- **Unit Tests (70%)**: Spring 없이 순수 Java로 빠르게
- **Integration Tests (20%)**: 슬라이스 테스트로 필요한 부분만
- **E2E Tests (10%)**: 전체 시스템 테스트는 최소화

---

## 5. 체크리스트

- [ ] POJO 테스트에 `@SpringBootTest` 사용하지 않았는가?
- [ ] Service 테스트에 Mockito를 사용했는가?
- [ ] Controller 테스트에 `@WebMvcTest`를 사용했는가?
- [ ] Repository 테스트에 `@DataJpaTest`를 사용했는가?
- [ ] 매직 넘버 대신 상수를 사용했는가?
- [ ] 테스트명이 무엇을 테스트하는지 설명하는가?
- [ ] 관련 테스트가 `@Nested`로 그룹화되었는가?
- [ ] 반복적인 테스트는 `@ParameterizedTest`로 작성했는가?
- [ ] Given-When-Then 구조가 명확한가?

---

## 6. 참고 파일

| 파일 | 설명 |
|------|------|
| `todoFix/MessyPaymentTest.java` | 개선 전 (문제점 예시) |
| `unit/policy/DiscountPolicyTest.java` | 단위 테스트 예시 |
| `unit/policy/TaxPolicyTest.java` | @Nested 활용 예시 |
| `unit/entity/PaymentEntityTest.java` | 엔티티 단위 테스트 |
| `unit/service/PaymentServiceTest.java` | Service 단위 테스트 (Mock 활용) |
| `integration/repository/PaymentRepositoryTest.java` | @DataJpaTest 예시 |
| `web/PaymentControllerTest.java` | @WebMvcTest 예시 |
