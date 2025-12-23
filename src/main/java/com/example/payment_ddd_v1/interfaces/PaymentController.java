package com.example.payment_ddd_v1.interfaces;

import com.example.payment_ddd_v1.application.PaymentService;
import com.example.payment_ddd_v1.domain.model.Payment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PaymentController - REST API 컨트롤러
 *
 * [Interfaces 계층]
 * - 외부 요청을 받아 Application 계층에 전달
 * - DTO 변환 담당
 * - HTTP 상태 코드 결정
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResult> createPayment(@RequestBody PaymentRequest request) {
        Payment payment = paymentService.createPayment(request);
        return ResponseEntity.ok(PaymentResult.from(payment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResult> getPayment(@PathVariable Long id) {
        Payment payment = paymentService.getPayment(id);
        return ResponseEntity.ok(PaymentResult.from(payment));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<PaymentResult> completePayment(@PathVariable Long id) {
        Payment payment = paymentService.completePayment(id);
        return ResponseEntity.ok(PaymentResult.from(payment));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResult> refundPayment(@PathVariable Long id) {
        Payment payment = paymentService.refundPayment(id);
        return ResponseEntity.ok(PaymentResult.from(payment));
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<PaymentResult> failPayment(@PathVariable Long id) {
        Payment payment = paymentService.failPayment(id);
        return ResponseEntity.ok(PaymentResult.from(payment));
    }
}
