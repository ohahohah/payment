# Payment Step3 - 애그리게이트 이후의 불편함 체험

## 목표

- DDD를 한다는 것은 프레임워크를 더 붙이는 일이 아니라, **도메인의 언어와 경계를 코드에 맞추는 작업**임
- **애그리게이트 다음에 구조(Architecture)를 고민하게 되는 이유**를 체감

---

## 요구사항 요약

### 요구사항 1 - 결제 승인 실패 사유 저장

```
실패 유형:
- 카드 한도 초과 (CARD_LIMIT_EXCEEDED)
- 네트워크 오류 (NETWORK_ERROR)
- 정책상 거절 (POLICY_REJECTED)

저장 내용:
- 실패 유형
- 실패 시점의 결제 금액
- 실패 시점의 정책 정보
```

### 요구사항 2 - 실패 시 외부 알림 시스템 연동

```
조건:
- 알림 전송 실패가 결제에 영향 주면 안 됨
- 알림 시스템은 추후 교체 가능
- 알림 내용 포맷은 정책에 따라 달라짐
```

### 요구사항 3 - 정책 변경으로 결제 흐름 분기

```
분기 조건:
- VIP 고객은 실패 시 재시도 (최대 3회)
- US 결제는 별도 정책 (고액 거절)
- 정책 거절은 즉시 종료 (재시도 없음)
```

---

## 패키지 구조

```
payment_step3/
├── PaymentProcessor.java           # 모든 책임을 담은 처리기 (의도적)
├── PaymentApprovalResult.java      # 승인 결과
└── domain/
    └── model/
        ├── Payment.java            # 결제 애그리게이트
        ├── PaymentStatus.java      # 결제 상태
        ├── FailureType.java        # 실패 유형
        ├── PaymentFailureRecord.java  # 실패 이력
        ├── Money.java              # 금액 VO
        └── Country.java            # 국가 VO
```

---

## 실행 방법

```java
PaymentProcessor processor = new PaymentProcessor();

// 결제 생성
Payment payment = processor.createPayment(10000, "KR", true);

// 결제 승인 시도
PaymentApprovalResult result = processor.approve(payment.getId());

if (result.isSuccess()) {
    System.out.println("승인 성공: " + result.getPayment().getStatus());
} else {
    System.out.println("승인 실패: " + result.getFailureType());
    System.out.println("실패 이력: " + result.getFailureRecord().getPolicyInfo());
}
```

---

## 테스트 실행

```bash
./gradlew test --tests "com.example.payment_step3.*"
```

---

## 구현 시 던진 질문들

### 요구사항 1 관련

| 질문 | 현재 구현 | 불편함 |
|------|----------|--------|
| 실패 사유는 Payment의 상태인가? | 별도 PaymentFailureRecord | Payment와 FailureRecord의 관계가 모호 |
| Payment 안에 실패 이력을 들고 있어야 하는가? | Processor에서 별도 저장 | 저장소가 분리되어 있음 |
| 실패 이력은 불변식인가, 기록용 데이터인가? | 기록용으로 처리 | 애그리게이트 경계가 불명확 |

### 요구사항 2 관련

| 질문 | 현재 구현 | 불편함 |
|------|----------|--------|
| 외부 알림 호출을 Payment 안에서 해도 되는가? | Processor에서 호출 | 도메인이 외부 시스템에 의존 |
| Processor에서 호출하면 괜찮은가? | 내부 클래스로 구현 | Mock 어려움, 테스트 불편 |
| 도메인이 외부 시스템을 알게 되면? | 직접 호출 | 교체 어려움, 의존성 증가 |

### 요구사항 3 관련

| 질문 | 현재 구현 | 불편함 |
|------|----------|--------|
| 분기 로직은 도메인 규칙인가, 흐름 제어인가? | Processor에 if문 | 정책 추가마다 코드 수정 |
| 애그리게이트에 조건 분기를 넣는 것이 맞는가? | 넣지 않음 | 어디에 둬야 할지 모호 |
| Processor가 정책 판단까지 맡으면? | 모든 분기 Processor에 | 책임 과부하, 테스트 폭발 |

---

## 느끼는 불편함 정리

### PaymentProcessor의 과도한 책임

```java
@Service
public class PaymentProcessor {
    // 담당 책임 (너무 많음!)
    // - 결제 생성 및 저장
    // - 결제 승인 시도
    // - 실패 사유 저장
    // - 외부 알림 전송
    // - 정책별 분기 처리
    // - VIP 재시도 로직
    // - 국가별 정책 적용
}
```

### 테스트 불편함

1. **Mock이 어려움**
   - NotificationClient가 내부 클래스
   - 의존성 주입 불가

2. **비결정적 테스트**
   - 네트워크 오류 10% 확률
   - 테스트 결과 불안정

3. **테스트 케이스 폭발**
   - VIP × 국가 × 실패유형 조합

### 코드 변경 범위

정책 추가 시 Processor 전체 수정 필요:
```java
// 새로운 국가 정책 추가?
if (payment.getCountry().isUS()) { ... }
if (payment.getCountry().isJP()) { ... }  // 추가
if (payment.getCountry().isEU()) { ... }  // 추가

// VIP 등급 세분화?
if (payment.isVip()) { ... }
if (payment.isVvip()) { ... }  // 추가
```

---

## Stop & Think

### 실습 중 기록할 것

- [ ] 애그리게이트에 넣기 찝찝했던 로직
- [ ] Processor가 맡게 된 과도한 책임
- [ ] 테스트 작성이 어려웠던 이유
- [ ] 어디에 두고 싶었지만 둘 수 없었던 코드

### 실습 후 질문

- 이 로직은 Payment의 규칙인가?
- 아니면 Payment를 사용하는 흐름의 문제인가?
- 애그리게이트를 더 키우는 게 답일까?
- 아니면 구조적으로 보호할 장치가 필요한가?

---

## 다음 단계 힌트

이 불편함을 해결하기 위한 패턴들:

1. **Application Service** - 유스케이스 흐름 분리
2. **Domain Service** - 여러 애그리게이트 걸친 로직
3. **Domain Event** - 부수 효과(알림) 분리
4. **Policy Pattern** - 정책 분기 캡슐화
5. **Port & Adapter** - 외부 시스템 의존성 역전

---

## 결론

> **이 요구사항에는 깔끔한 단일 정답 구조가 없다.**
> **불편함을 느끼는 것이 정상이다.**
> **이 불편함이 바로 Architectural DDD로 넘어가는 신호다.**
