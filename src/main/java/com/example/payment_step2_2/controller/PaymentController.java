package com.example.payment_step2_2.controller;

import com.example.payment_step2_2.dto.PaymentRequest;
import com.example.payment_step2_2.dto.PaymentResult;
import com.example.payment_step2_2.entity.Payment;
import com.example.payment_step2_2.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PaymentController - 결제 REST API
 *
 * [payment_step1과 동일 - 변경 없음]
 *
 * Controller는 HTTP 요청/응답 처리만 담당.
 * 비즈니스 로직이 Service에서 Entity로 이동했지만
 * Controller는 Service만 호출하므로 변경 불필요.
 */
@RestController
@RequestMapping("/api/step2_2/payments")
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
