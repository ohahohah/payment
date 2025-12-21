package com.example.payment_ddd.interfaces.rest;

import com.example.payment_ddd.application.command.CreatePaymentCommand;
import com.example.payment_ddd.application.command.RefundPaymentCommand;
import com.example.payment_ddd.application.service.PaymentCommandService;
import com.example.payment_ddd.domain.model.Payment;
import com.example.payment_ddd.interfaces.dto.PaymentRequest;
import com.example.payment_ddd.interfaces.dto.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PaymentDddController - DDD 결제 REST 컨트롤러
 *
 * [Interfaces 레이어 역할]
 * - 외부 요청(HTTP)을 내부 명령(Command)으로 변환
 * - 도메인 결과를 외부 응답(DTO)으로 변환
 * - 얇은 레이어: 비즈니스 로직 없음
 *
 * [계층 흐름]
 * HTTP Request → Controller → Command → Application Service → Domain
 * Domain → Application Service → Domain Object → Controller → DTO → HTTP Response
 */
@RestController
@RequestMapping("/api/v2/payments")
public class PaymentDddController {

    private final PaymentCommandService paymentCommandService;

    public PaymentDddController(PaymentCommandService paymentCommandService) {
        this.paymentCommandService = paymentCommandService;
    }

    /**
     * 결제 생성 및 완료
     *
     * POST /api/v2/payments
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        // DTO → Command 변환
        CreatePaymentCommand command = new CreatePaymentCommand(
                request.amount(),
                request.country(),
                request.isVip()
        );

        // Application Service 호출
        Payment payment = paymentCommandService.createAndCompletePayment(command);

        // Domain → DTO 변환
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    /**
     * 결제 조회
     *
     * GET /api/v2/payments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        Payment payment = paymentCommandService.getPayment(id);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    /**
     * 모든 결제 조회
     *
     * GET /api/v2/payments
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<Payment> payments = paymentCommandService.getAllPayments();
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 결제 환불
     *
     * POST /api/v2/payments/{id}/refund
     */
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long id) {
        RefundPaymentCommand command = new RefundPaymentCommand(id);
        Payment payment = paymentCommandService.refundPayment(command);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
}
