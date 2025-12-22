package com.example.payment_ul.controller;

import com.example.payment_ul.dto.PaymentRequest;
import com.example.payment_ul.dto.PaymentResponse;
import com.example.payment_ul.dto.PaymentResult;
import com.example.payment_ul.entity.Payment;
import com.example.payment_ul.entity.PaymentStatus;
import com.example.payment_ul.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ====================================================================
 * PaymentController - 결제 API 컨트롤러 (유비쿼터스 랭귀지 적용)
 * ====================================================================
 *
 * [독립 패키지]
 * - payment_ul 패키지 전용 Controller입니다
 * - Qualifier 없이 독립적으로 동작합니다
 *
 * [API 엔드포인트]
 * - POST   /api/payments          결제 생성
 * - GET    /api/payments/{id}     결제 조회
 * - GET    /api/payments          전체 조회
 * - GET    /api/payments/status   상태별 조회
 * - PATCH  /api/payments/{id}/refund  환불
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResult> createPayment(@RequestBody PaymentRequest request) {
        PaymentResult result = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        Payment payment = paymentService.getPayment(id);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@RequestParam PaymentStatus status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long id) {
        Payment payment = paymentService.refundPayment(id);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<PaymentResponse>> getRecentPayments(
            @RequestParam(defaultValue = "10") int limit) {
        List<Payment> payments = paymentService.getRecentPayments(limit);
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleError(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
