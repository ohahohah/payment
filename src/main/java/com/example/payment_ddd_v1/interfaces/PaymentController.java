package com.example.payment_ddd_v1.interfaces;

import com.example.payment_ddd_v1.application.PaymentService;
import com.example.payment_ddd_v1.domain.model.Payment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<PaymentDto.Response> createPayment(@RequestBody PaymentDto.Request request) {
        Payment payment = paymentService.createPayment(
                request.amount(),
                request.country(),
                request.isVip()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaymentDto.Response.from(payment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto.Response> getPayment(@PathVariable Long id) {
        Payment payment = paymentService.getPayment(id);
        return ResponseEntity.ok(PaymentDto.Response.from(payment));
    }

    @GetMapping
    public ResponseEntity<List<PaymentDto.Response>> getAllPayments() {
        List<PaymentDto.Response> responses = paymentService.getAllPayments()
                .stream()
                .map(PaymentDto.Response::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/refund")
    public ResponseEntity<PaymentDto.Response> refundPayment(@PathVariable Long id) {
        Payment payment = paymentService.refundPayment(id);
        return ResponseEntity.ok(PaymentDto.Response.from(payment));
    }
}
