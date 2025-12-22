package com.example.payment.controller;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.dto.PaymentResult;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ====================================================================
 * PaymentController - REST API 컨트롤러 (웹 요청 처리)
 * ====================================================================
 *
 * [Controller 계층이란?]
 * - HTTP 요청을 받아서 처리하고 응답을 반환하는 계층입니다
 * - 클라이언트(브라우저, 앱)와 서버 사이의 인터페이스 역할
 * - 비즈니스 로직은 Service에 위임합니다
 *
 * [REST API란?]
 * - REpresentational State Transfer
 * - HTTP 메서드로 동작을 표현:
 *   - GET: 조회 (Read)
 *   - POST: 생성 (Create)
 *   - PUT: 전체 수정 (Update)
 *   - PATCH: 부분 수정 (Partial Update)
 *   - DELETE: 삭제 (Delete)
 *
 * [@RestController]
 * - @Controller + @ResponseBody의 조합
 * - 모든 메서드의 반환값이 자동으로 JSON으로 변환됩니다
 * - 뷰(HTML)가 아닌 데이터(JSON)를 반환할 때 사용
 *
 * [@RequestMapping("/api/payments")]
 * - 이 컨트롤러의 모든 엔드포인트에 공통 접두사 설정
 * - 예: /api/payments, /api/payments/{id} 등
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * [생성자 주입]
     * - PaymentService를 주입받습니다
     * - @Autowired 생략 가능 (생성자가 하나일 때)
     */
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * [결제 생성 API] - POST /api/payments
     *
     * [새 결제를 생성합니다]
     * - 요청 본문(body)에서 JSON으로 결제 정보를 받습니다
     * - 할인과 세금을 적용한 결과를 반환합니다
     *
     * [@PostMapping]
     * - HTTP POST 요청을 처리합니다
     * - 주로 새로운 리소스 생성에 사용
     *
     * [@RequestBody]
     * - HTTP 요청 본문(body)의 JSON을 Java 객체로 변환
     * - Jackson 라이브러리가 자동으로 매핑합니다
     *
     * [ResponseEntity]
     * - HTTP 응답을 세밀하게 제어할 수 있습니다
     * - 상태 코드, 헤더, 본문을 설정 가능
     *
     * [HTTP 상태 코드]
     * - 201 Created: 리소스 생성 성공
     * - 200 OK: 일반적인 성공
     * - 400 Bad Request: 잘못된 요청
     * - 404 Not Found: 리소스 없음
     *
     * @param request 결제 요청 정보 (amt1, cd, flag)
     * @return 201 Created + 결제 결과
     */
    @PostMapping
    public ResponseEntity<PaymentResult> process(@RequestBody PaymentRequest request) {
        PaymentResult result = paymentService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * [결제 단건 조회 API] - GET /api/payments/{id}
     *
     * [ID로 결제 정보를 조회합니다]
     *
     * [@GetMapping("/{id}")]
     * - HTTP GET 요청을 처리합니다
     * - {id}는 경로 변수(Path Variable)입니다
     *
     * [@PathVariable]
     * - URL 경로의 일부를 변수로 추출합니다
     * - 예: /api/payments/123 → id = 123
     *
     * [PaymentResponse.from()]
     * - 엔티티를 응답 DTO로 변환합니다
     * - 엔티티를 직접 노출하지 않고 DTO를 사용하는 이유:
     *   1. API 스펙과 내부 구조 분리
     *   2. 민감한 정보 숨기기
     *   3. API 버전 관리 용이
     *
     * @param id 조회할 결제 ID
     * @return 200 OK + 결제 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> get(@PathVariable Long id) {
        Payment payment = paymentService.getData(id);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    /**
     * [전체 결제 조회 API] - GET /api/payments
     *
     * [모든 결제 목록을 조회합니다]
     *
     * [List를 반환하면]
     * - JSON 배열로 자동 변환됩니다
     * - 예: [{"id": 1, ...}, {"id": 2, ...}]
     *
     * [.toList()]
     * - Java 16+ 의 Stream API 메서드
     * - collect(Collectors.toList())와 동일
     *
     * @return 200 OK + 결제 목록
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAll() {
        List<Payment> payments = paymentService.getList();
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * [상태별 조회 API] - GET /api/payments/status?stat=C
     *
     * [특정 상태의 결제만 조회합니다]
     *
     * [@RequestParam]
     * - URL 쿼리 파라미터를 추출합니다
     * - 예: /api/payments/status?stat=C → stat = C
     *
     * [PaymentStatus Enum 자동 변환]
     * - Spring이 문자열 "C"를 PaymentStatus.C로 자동 변환
     * - 잘못된 값이 오면 400 Bad Request 응답
     *
     * @param stat 조회할 상태 (P, C, F, R)
     * @return 200 OK + 해당 상태의 결제 목록
     */
    @GetMapping("/status")
    public ResponseEntity<List<PaymentResponse>> getByStat(@RequestParam PaymentStatus stat) {
        List<Payment> payments = paymentService.getListByStat(stat);
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * [결제 환불 API] - PATCH /api/payments/{id}/refund
     *
     * [완료된 결제를 환불 처리합니다]
     *
     * [@PatchMapping]
     * - HTTP PATCH 요청을 처리합니다
     * - 리소스의 부분 수정에 사용
     * - PUT은 전체 교체, PATCH는 부분 수정
     *
     * [RESTful URL 설계]
     * - /api/payments/{id}/refund
     * - 리소스(payments) + ID + 동작(refund)
     *
     * @param id 환불할 결제 ID
     * @return 200 OK + 환불된 결제 정보
     */
    @PatchMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> update(@PathVariable Long id) {
        Payment payment = paymentService.updateStatus(id);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    /**
     * [최근 결제 조회 API] - GET /api/payments/recent?limit=10
     *
     * [최근 N건의 결제를 조회합니다]
     *
     * [@RequestParam(defaultValue = "10")]
     * - 파라미터가 없으면 기본값 10을 사용합니다
     * - 예: /api/payments/recent → limit = 10
     * - 예: /api/payments/recent?limit=5 → limit = 5
     *
     * @param limit 조회할 건수 (기본값: 10)
     * @return 200 OK + 최근 결제 목록
     */
    @GetMapping("/recent")
    public ResponseEntity<List<PaymentResponse>> getRecent(
            @RequestParam(defaultValue = "10") int limit) {
        List<Payment> payments = paymentService.getRecent(limit);
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * [예외 처리] - IllegalArgumentException 발생 시
     *
     * [@ExceptionHandler]
     * - 이 컨트롤러에서 발생한 특정 예외를 처리합니다
     * - IllegalArgumentException 발생 시 이 메서드가 호출됨
     *
     * [400 Bad Request]
     * - 클라이언트의 잘못된 요청을 의미합니다
     * - 예: 존재하지 않는 ID로 조회, 잘못된 파라미터 등
     *
     * [전역 예외 처리]
     * - @ControllerAdvice를 사용하면 모든 컨트롤러에서
     *   발생하는 예외를 한 곳에서 처리할 수 있습니다
     *
     * @param e 발생한 예외
     * @return 400 Bad Request + 에러 메시지
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleError(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
