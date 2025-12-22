package com.example.payment_step2.controller;

import com.example.payment_step2.dto.PaymentRequest;
import com.example.payment_step2.dto.PaymentResult;
import com.example.payment_step2.entity.Payment;
import com.example.payment_step2.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PaymentController - 결제 REST API
 *
 * [payment_ul과 동일한 API 구조]
 * - DTO는 primitive 타입 유지 (외부 API 호환성)
 * - Service에서 Value Object로 변환
 */
@RestController
@RequestMapping("/api/step1/payments")
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
    public ResponseEntity<Payment> getPayment(@PathVariable Long id) {
        Payment payment = paymentService.getPayment(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @PatchMapping("/{id}/refund")
    public ResponseEntity<Payment> refundPayment(@PathVariable Long id) {
        Payment payment = paymentService.refundPayment(id);
        return ResponseEntity.ok(payment);
    }
}
