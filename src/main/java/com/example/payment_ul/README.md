# 유비쿼터스 랭귀지 (Ubiquitous Language) 패키지

## 개요

이 패키지(`com.example.payment_ul`)는 **유비쿼터스 랭귀지**를 적용한 결제 시스템입니다.
`com.example.payment` 패키지의 안티패턴 네이밍을 도메인 용어로 변경하여 가독성과 의사소통을 개선했습니다.

---

## 독립 실행

이 패키지는 **독립적으로 실행 가능**합니다.

```bash
# 유비쿼터스 랭귀지 애플리케이션 실행
./gradlew runPaymentUL

# 또는
./gradlew bootRun -PmainClass=com.example.payment_ul.PaymentULApplication
```

---

## 패키지 구조

```
com.example.payment_ul/
├── PaymentULApplication.java          # 독립 실행 가능한 메인 클래스
├── entity/
│   ├── Payment.java                   # 결제 엔티티
│   └── PaymentStatus.java             # 결제 상태 Enum
├── dto/
│   ├── PaymentRequest.java            # 요청 DTO
│   ├── PaymentResult.java             # 결과 DTO
│   └── PaymentResponse.java           # 응답 DTO
├── repository/
│   └── PaymentRepository.java         # 저장소
├── service/
│   └── PaymentService.java            # 서비스
├── controller/
│   └── PaymentController.java         # 컨트롤러
├── handler/
│   ├── PaymentCompletionHandler.java  # 결제 완료 처리기 인터페이스
│   ├── PaymentAuditLogger.java        # 결제 감사 로그 기록기
│   └── SettlementRequestHandler.java  # 정산 요청 처리기
└── policy/
    ├── discount/
    │   ├── CustomerDiscountPolicy.java  # 고객 할인 정책 인터페이스
    │   └── VipDiscountPolicy.java       # VIP 할인 정책
    └── tax/
        ├── TaxPolicy.java               # 세금 정책 인터페이스
        ├── KoreaVatPolicy.java          # 한국 부가세 정책
        └── UsSalesTaxPolicy.java        # 미국 판매세 정책
```

---

## 유비쿼터스 랭귀지란?

- **도메인 전문가**와 **개발자**가 공통으로 사용하는 언어입니다
- 코드, 문서, 대화에서 **같은 용어**를 사용합니다
- 모호함을 줄이고 **의사소통을 명확**하게 합니다
- **DDD(도메인 주도 설계)**의 핵심 개념입니다

---

## 변경 비교표

### 1. PaymentStatus (결제 상태)

| 변경 전 | 변경 후    | 설명        |
|---------|------------|-------------|
| P       | PENDING    | 결제 대기   |
| C       | COMPLETED  | 결제 완료   |
| F       | FAILED     | 결제 실패   |
| R       | REFUNDED   | 환불 완료   |

### 2. Payment Entity (결제 엔티티 필드)

| 변경 전 | 변경 후          | 설명                    |
|---------|------------------|-------------------------|
| amt1    | originalPrice    | 원래 가격               |
| amt2    | discountedAmount | 할인 적용 후 금액       |
| amt3    | taxedAmount      | 세금 적용 후 최종 금액  |
| cd      | country          | 국가 코드               |
| flag    | isVip            | VIP 고객 여부           |
| stat    | status           | 결제 상태               |
| cdt     | createdAt        | 생성 일시               |
| udt     | updatedAt        | 수정 일시               |

### 3. PaymentService (서비스 메서드)

| 변경 전         | 변경 후               | 설명              |
|-----------------|----------------------|-------------------|
| execute()       | processPayment()     | 결제 처리         |
| getData()       | getPayment()         | 결제 단건 조회    |
| getList()       | getAllPayments()     | 전체 결제 조회    |
| getListByStat() | getPaymentsByStatus()| 상태별 결제 조회  |
| updateStatus()  | refundPayment()      | 결제 환불         |

### 4. 클래스명 (도메인 용어 적용)

#### 할인 정책

| 변경 전 (패턴 용어) | 변경 후 (도메인 용어) | 설명 |
|---------------------|----------------------|------|
| DiscountStrategy | CustomerDiscountPolicy | 고객 할인 정책 인터페이스 |
| DefaultDiscountStrategy | VipDiscountPolicy | VIP/일반 고객별 할인 정책 |

#### 세금 정책

| 변경 전 (패턴 용어) | 변경 후 (도메인 용어) | 설명 |
|---------------------|----------------------|------|
| TaxStrategy | TaxPolicy | 세금 정책 인터페이스 |
| KoreaTaxStrategy | KoreaVatPolicy | 한국 부가가치세 정책 (10%) |
| UsTaxStrategy | UsSalesTaxPolicy | 미국 판매세 정책 (7%) |

#### 결제 완료 처리

| 변경 전 (패턴 용어) | 변경 후 (도메인 용어) | 설명 |
|---------------------|----------------------|------|
| PaymentObserver | PaymentCompletionHandler | 결제 완료 처리기 인터페이스 |
| LoggingObserver | PaymentAuditLogger | 결제 감사 로그 기록기 |
| SettlementObserver | SettlementRequestHandler | 정산 요청 처리기 |

---

## API 엔드포인트

| 메서드 | 엔드포인트                        | 설명           |
|--------|-----------------------------------|----------------|
| POST   | /api/payments                     | 결제 생성      |
| GET    | /api/payments/{id}                | 결제 단건 조회 |
| GET    | /api/payments                     | 전체 결제 조회 |
| GET    | /api/payments/status?status=COMPLETED | 상태별 조회 |
| PATCH  | /api/payments/{id}/refund         | 결제 환불      |
| GET    | /api/payments/recent?limit=10     | 최근 결제 조회 |

---

## JSON 예시

### 요청 (PaymentRequest)

```json
{
  "originalPrice": 10000,
  "country": "KR",
  "isVip": true
}
```

### 응답 (PaymentResponse)

```json
{
  "id": 1,
  "originalPrice": 10000,
  "discountedAmount": 8500,
  "taxedAmount": 9350,
  "country": "KR",
  "isVip": true,
  "status": "COMPLETED",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

---

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# payment_ul 테스트만 실행
./gradlew test --tests "com.example.payment_ul.*"
```

---

## 패키지 비교

| 항목 | com.example.payment | com.example.payment_ul |
|------|---------------------|------------------------|
| 목적 | 연습문제 (패턴 용어) | 정답 (도메인 용어) |
| 클래스명 | DiscountStrategy, PaymentObserver | CustomerDiscountPolicy, PaymentCompletionHandler |
| 필드명 | 축약어 (amt1, cd) | 명확한 이름 (originalPrice, country) |
| 메서드명 | 모호함 (execute) | 비즈니스 용어 (processPayment) |
| 상태값 | 코드 (P, C, R) | 의미 (PENDING, COMPLETED, REFUNDED) |
| 독립 실행 | PaymentApplication | PaymentULApplication |
