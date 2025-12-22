## Step 1. 보편 언어 주입 (Ubiquitous Language Injection)

### 활동 1-1. “코드가 업무 문서처럼 읽히는가?” 점검 (팀별)
- 아래 질문에 답하면서 타겟 클래스 1~2개를 선정함.
    - 이 클래스/인터페이스 이름을 읽고, 비개발자(현업)에게 설명 가능함?
    - 이 이름이 기술(패턴/프레임워크) 용어에 기대고 있지는 않음?
    - 이 이름이 “무엇을 하는지”가 아니라 “어떻게 구현했는지”를 말하고 있지는 않음?

- 선정 기준
    - Strategy / Observer / Listener / Handler 같은 구현 방식이 이름에 들어감
    - Discount / Fee / Tax 같은 계산 종류만 있고 도메인 맥락이 없음
    - Processor / Manager / Util 같은 범용 단어가 들어감

- 위 기준 중 둘 이상에 해당하면 리네이밍 대상임.

### 활동 1-2. 도메인 용어 사전 만들기 (팀별)
- 팀에서 10분 동안 도메인 용어 후보를 만들고, 5분 동안 합의함.

- 토론 규칙.
    - 기술 단어 금지  
      Strategy, Observer, Handler, Service 같은 단어는 최후 수단임
    - 업무 문장으로 바꿔 말하기  
      “할인 전략” 대신 “운임 정책”처럼 표현
    - 시간 / 조건 / 대상 중심으로 이름 붙이기
        - 대상: 고객, 주문, 결제, 정산, 알림, 정책
        - 조건: VIP, 국가, 기간, 프로모션, 주말, 수수료
        - 결과: 청구 금액, 과금, 승인, 실패, 알림 발송

- 형태 예시.
    - DiscountStrategy → CustomerDiscountPolicy
    - TaxStrategy → TaxPolicy
    - LoggingObserver → PaymentAuditLogger
    - SettlementObserver → SettlementRequestHandler

---

## 실습: com.example.payment → com.example.payment_ul

### 변경 대상 분석

#### 할인 정책 (policy/discount)

| 변경 전 (패턴 용어) | 변경 후 (도메인 용어) | 설명 |
|---------------------|----------------------|------|
| DiscountStrategy | CustomerDiscountPolicy | 고객 할인 정책 인터페이스 |
| DefaultDiscountStrategy | VipDiscountPolicy | VIP/일반 고객별 할인 정책 |

#### 세금 정책 (policy/tax)

| 변경 전 (패턴 용어) | 변경 후 (도메인 용어) | 설명 |
|---------------------|----------------------|------|
| TaxStrategy | TaxPolicy | 세금 정책 인터페이스 |
| KoreaTaxStrategy | KoreaVatPolicy | 한국 부가가치세 정책 (10%) |
| UsTaxStrategy | UsSalesTaxPolicy | 미국 판매세 정책 (7%) |

#### 결제 완료 처리 (handler)

| 변경 전 (패턴 용어) | 변경 후 (도메인 용어) | 설명 |
|---------------------|----------------------|------|
| PaymentObserver | PaymentCompletionHandler | 결제 완료 처리기 인터페이스 |
| LoggingObserver | PaymentAuditLogger | 결제 감사 로그 기록기 |
| SettlementObserver | SettlementRequestHandler | 정산 요청 처리기 |

### DTO 필드명 변경

| 변경 전 | 변경 후 | 의미 |
|---------|---------|------|
| amt1 | originalPrice | 원래 가격 |
| amt2 | discountedAmount | 할인 적용 후 금액 |
| amt3 | taxedAmount | 세금 적용 후 최종 금액 |
| cd | country | 국가 코드 |
| flag | isVip | VIP 고객 여부 |

### 메서드명 변경

| 변경 전 | 변경 후 | 의미 |
|---------|---------|------|
| execute() | processPayment() | 결제 처리 |
| getData() | getPayment() | 결제 조회 |
| getList() | getAllPayments() | 전체 결제 목록 |
| updateStatus() | refundPayment() | 환불 처리 |
| getSum() | getTotalAmount() | 총액 조회 |
| getRecent() | getRecentPayments() | 최근 결제 목록 |

### Entity 필드명 변경

| 변경 전 | 변경 후 |
|---------|---------|
| stat | status |
| cdt | createdAt |
| udt | updatedAt |

### PaymentStatus Enum 값 변경

| 변경 전 | 변경 후 |
|---------|---------|
| P | PENDING |
| C | COMPLETED |
| F | FAILED |
| R | REFUNDED |

### 활동 1-3. 코드 구현: 리네이밍 + 테스트 깨짐 최소화

- 구현 목표.
    - 클래스/인터페이스 이름을 도메인 용어로 교체
    - 테스트와 패키지 구조를 크게 흔들지 않음
    - **의미는 그대로 두고 이름만 변경**

- 구현 Tip.
- IDE 리팩토링 Rename 기능만 사용
    - 파일명 변경
    - 클래스명 변경
    - 참조 자동 반영
- 이름 변경 체크리스트
    - 인터페이스 이름이 도메인 의미를 담는가?
    - 구현체 이름이 “도메인 + 조건” 형태로 확장 가능한가?
    - 메서드 이름이 도메인 문장처럼 읽히는가?

- 빠른 검증.
    - 전체 테스트 실행
    - 실패 시 원인 분리
        - 이름 누락: import, 패키지 경로, 클래스 참조
        - 의미 변경: 메서드 시그니처/동작 변경 여부
