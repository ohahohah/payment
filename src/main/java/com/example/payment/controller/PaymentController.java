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
 * PaymentController - 결제 REST API 컨트롤러
 * ====================================================================
 *
 * [컨트롤러 (Controller)란?]
 * - MVC 패턴에서 사용자의 요청을 받아 처리하는 계층입니다
 * - HTTP 요청을 받아서 Service를 호출하고, 응답을 반환합니다
 * - 비즈니스 로직은 Service에 위임하고, 컨트롤러는 요청/응답 처리만 담당합니다
 *
 * [@RestController 어노테이션]
 * - @Controller + @ResponseBody의 조합입니다
 * - 이 클래스의 모든 메서드 반환값이 HTTP 응답 본문으로 직접 전송됩니다
 * - 뷰(View)를 반환하는 대신 데이터(JSON)를 반환합니다
 *
 * [@RequestMapping 어노테이션]
 * - 이 컨트롤러가 처리할 URL 경로의 공통 접두사를 지정합니다
 * - "/api/payments" 경로로 시작하는 요청을 이 컨트롤러가 처리합니다
 *
 * [REST API 설계 원칙]
 * - 자원(Resource)을 URL로 표현: /api/payments
 * - 행위(Verb)를 HTTP 메서드로 표현:
 *   - GET: 조회
 *   - POST: 생성
 *   - PUT: 전체 수정
 *   - PATCH: 부분 수정
 *   - DELETE: 삭제
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 생성자 주입
     *
     * [PaymentService 주입]
     * - 이전: PaymentProcessor 직접 주입
     * - 현재: PaymentService 주입 (Service가 Processor를 사용)
     *
     * [레이어 구조]
     * Controller → Service → Repository
     *                 ↓
     *              Processor
     *
     * @param paymentService 결제 서비스
     */
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 결제 처리 API
     *
     * [@PostMapping]
     * - HTTP POST 요청을 처리합니다
     * - 결과: POST /api/payments
     *
     * [@RequestBody]
     * - HTTP 요청 본문(Body)의 JSON을 자바 객체로 변환합니다
     *
     * [HTTP 상태 코드]
     * - 201 Created: 리소스가 성공적으로 생성됨
     * - POST로 데이터 생성 시 201이 적절합니다
     *
     * @param request 결제 요청 (JSON)
     * @return 201 Created + 결제 결과
     */
    @PostMapping
    public ResponseEntity<PaymentResult> processPayment(@RequestBody PaymentRequest request) {
        PaymentResult result = paymentService.processPayment(request);

        // ResponseEntity로 상태 코드와 본문을 명시적으로 설정
        return ResponseEntity
                .status(HttpStatus.CREATED)  // 201 Created
                .body(result);
    }

    /**
     * 결제 단건 조회 API
     *
     * [@GetMapping("/{id}")]
     * - HTTP GET 요청을 처리합니다
     * - {id}: 경로 변수 (Path Variable)
     * - 결과: GET /api/payments/1, GET /api/payments/2, ...
     *
     * [@PathVariable]
     * - URL 경로의 {id} 값을 메서드 파라미터로 매핑합니다
     * - /api/payments/123 → id = 123
     *
     * [DTO 변환]
     * - 엔티티를 직접 반환하지 않고 DTO(PaymentResponse)로 변환
     * - 이유:
     *   1. 엔티티 구조 변경이 API에 영향을 주지 않음
     *   2. 필요한 필드만 노출 (보안)
     *   3. 순환 참조 방지 (연관 관계)
     *
     * @param id 결제 ID
     * @return 200 OK + 결제 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        Payment payment = paymentService.getPayment(id);
        // 엔티티 → DTO 변환
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    /**
     * 전체 결제 목록 조회 API
     *
     * [List 반환]
     * - JSON 배열로 직렬화됩니다
     * - [{ payment1 }, { payment2 }, ...]
     *
     * @return 200 OK + 결제 목록
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();

        // Stream API로 엔티티 리스트 → DTO 리스트 변환
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)  // 메서드 참조
                .toList();                   // Java 16+ (또는 .collect(Collectors.toList()))

        return ResponseEntity.ok(responses);
    }

    /**
     * 상태별 결제 조회 API
     *
     * [@RequestParam]
     * - 쿼리 파라미터를 메서드 파라미터로 매핑합니다
     * - /api/payments/status?status=COMPLETED → status = COMPLETED
     *
     * [Enum 변환]
     * - 스프링이 문자열 "COMPLETED"를 PaymentStatus.COMPLETED로 자동 변환
     *
     * @param status 조회할 결제 상태
     * @return 200 OK + 해당 상태의 결제 목록
     */
    @GetMapping("/status")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @RequestParam PaymentStatus status) {

        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * 결제 환불 API
     *
     * [@PatchMapping]
     * - HTTP PATCH 요청을 처리합니다
     * - 리소스의 부분 수정에 사용 (vs PUT은 전체 수정)
     *
     * [RESTful 설계]
     * - PATCH /api/payments/{id}/refund
     * - 결제 리소스의 "상태"를 부분 수정
     *
     * @param id 환불할 결제 ID
     * @return 200 OK + 환불된 결제 정보
     */
    @PatchMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long id) {
        Payment payment = paymentService.refundPayment(id);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    /**
     * 최근 결제 조회 API
     *
     * [@RequestParam(defaultValue = "10")]
     * - 파라미터가 없으면 기본값 10 사용
     * - /api/payments/recent → limit = 10
     * - /api/payments/recent?limit=5 → limit = 5
     *
     * @param limit 조회할 건수 (기본값: 10)
     * @return 200 OK + 최근 결제 목록
     */
    @GetMapping("/recent")
    public ResponseEntity<List<PaymentResponse>> getRecentPayments(
            @RequestParam(defaultValue = "10") int limit) {

        List<Payment> payments = paymentService.getRecentPayments(limit);
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }
}
