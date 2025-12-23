package com.example.payment_ddd_v1_1.interfaces;

import com.example.payment_ddd_v1_1.application.PaymentService;
import com.example.payment_ddd_v1_1.domain.model.Payment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PaymentController - 결제 REST API
 *
 * [Interfaces 계층]
 * - HTTP 요청/응답 처리
 * - DTO 변환
 */
@RestController
@RequestMapping("/api/ddd/v1_1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        Payment payment = paymentService.createPayment(
                request.getAmount(),
                request.getCountryCode(),
                request.getIsVip()
        );
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        Payment payment = paymentService.getPayment(id);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<PaymentResponse> completePayment(@PathVariable Long id) {
        Payment payment = paymentService.completePayment(id);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long id) {
        Payment payment = paymentService.refundPayment(id);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<PaymentResponse> failPayment(@PathVariable Long id) {
        Payment payment = paymentService.failPayment(id);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
}
