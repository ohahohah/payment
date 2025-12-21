package com.example.payment.web;

import com.example.payment.controller.PaymentController;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.dto.PaymentResult;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================================
 * [GOOD] PaymentControllerTest - 컨트롤러 슬라이스 테스트
 * ============================================================================
 *
 * [@WebMvcTest]
 * - 웹 레이어(Controller)만 테스트하는 슬라이스 테스트
 * - MVC 관련 빈만 로딩 (Controller, ControllerAdvice, Filter 등)
 * - Service, Repository 등은 로딩하지 않음 → @MockBean으로 주입 필요
 *
 * [왜 @WebMvcTest를 사용하나요?]
 * 1. 컨트롤러 로직만 격리하여 테스트
 * 2. HTTP 요청/응답 검증에 집중
 * 3. @SpringBootTest보다 훨씬 가벼움 (빠른 테스트)
 * 4. Service 계층은 Mock으로 대체하여 독립적 테스트
 *
 * [슬라이스 테스트 비교]
 * - @WebMvcTest: Controller 레이어 (이 테스트)
 * - @DataJpaTest: Repository 레이어
 * - @JsonTest: JSON 직렬화/역직렬화
 * - @RestClientTest: REST 클라이언트
 *
 * [MockMvc]
 * - 실제 서버 없이 HTTP 요청을 시뮬레이션
 * - 요청 URL, 메서드, 파라미터, 바디 설정 가능
 * - 응답 상태코드, 헤더, 바디 검증 가능
 *
 * [@MockBean vs @Mock]
 * - @MockBean: 스프링 컨텍스트에 Mock 빈을 등록 (스프링 테스트용)
 * - @Mock: Mockito의 순수 Mock (스프링 없이 사용)
 *
 * 이 테스트에서는 @WebMvcTest가 스프링 컨텍스트를 사용하므로
 * @MockBean을 사용합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("PaymentController 웹 레이어 테스트")
class PaymentControllerTest {

    /**
     * [MockMvc]
     * - HTTP 요청을 시뮬레이션하는 테스트 도구
     * - 실제 서버(Tomcat)를 띄우지 않고 테스트 가능
     * - perform(): 요청 실행
     * - andExpect(): 응답 검증
     * - andDo(): 추가 작업 (로깅 등)
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * [@MockBean]
     * - 스프링 컨텍스트에 Mock 객체를 빈으로 등록
     * - @WebMvcTest는 Service를 로딩하지 않으므로 Mock으로 대체
     * - Controller가 의존하는 Service를 Mocking
     */
    @MockBean
    private PaymentService paymentService;

    /**
     * [ObjectMapper]
     * - JSON 직렬화/역직렬화 도구
     * - 테스트에서 요청 바디를 JSON으로 변환할 때 사용
     */
    @Autowired
    private ObjectMapper objectMapper;

    // 테스트 데이터 상수
    private static final Long PAYMENT_ID = 1L;
    private static final double ORIGINAL_PRICE = 10000.0;
    private static final double DISCOUNTED_AMOUNT = 8500.0;
    private static final double TAXED_AMOUNT = 9350.0;
    private static final String COUNTRY = "KR";

    @Nested
    @DisplayName("POST /api/payments - 결제 생성")
    class CreatePaymentTest {

        @Test
        @DisplayName("유효한 요청으로 결제를 생성하면 201 Created와 결제 결과를 반환한다")
        void shouldCreatePaymentSuccessfully() throws Exception {
            // Given - 요청 데이터
            PaymentRequest request = new PaymentRequest(ORIGINAL_PRICE, COUNTRY, true);

            // Mock 설정
            PaymentResult mockResult = new PaymentResult(
                    ORIGINAL_PRICE, DISCOUNTED_AMOUNT, TAXED_AMOUNT, COUNTRY, true
            );
            given(paymentService.processPayment(any(PaymentRequest.class)))
                    .willReturn(mockResult);

            // When & Then
            mockMvc.perform(post("/api/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())  // 요청/응답 로그 출력
                    .andExpect(status().isCreated())  // 201 Created
                    .andExpect(jsonPath("$.originalPrice").value(ORIGINAL_PRICE))
                    .andExpect(jsonPath("$.discountedAmount").value(DISCOUNTED_AMOUNT))
                    .andExpect(jsonPath("$.taxedAmount").value(TAXED_AMOUNT))
                    .andExpect(jsonPath("$.country").value(COUNTRY))
                    .andExpect(jsonPath("$.isVip").value(true));  // Record 필드명 그대로

            // Service 호출 검증
            then(paymentService)
                    .should(times(1))
                    .processPayment(any(PaymentRequest.class));
        }

        @Test
        @DisplayName("일반 고객 결제 요청도 정상 처리된다")
        void shouldCreatePaymentForNormalCustomer() throws Exception {
            // Given
            PaymentRequest request = new PaymentRequest(ORIGINAL_PRICE, COUNTRY, false);
            PaymentResult mockResult = new PaymentResult(
                    ORIGINAL_PRICE, 9000.0, 9900.0, COUNTRY, false
            );
            given(paymentService.processPayment(any(PaymentRequest.class)))
                    .willReturn(mockResult);

            // When & Then
            mockMvc.perform(post("/api/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())  // 201 Created
                    .andExpect(jsonPath("$.isVip").value(false))  // Record 필드명 그대로
                    .andExpect(jsonPath("$.discountedAmount").value(9000.0));
        }

        @Test
        @DisplayName("미국 결제 요청도 정상 처리된다")
        void shouldCreatePaymentForUS() throws Exception {
            // Given
            PaymentRequest request = new PaymentRequest(ORIGINAL_PRICE, "US", true);
            PaymentResult mockResult = new PaymentResult(
                    ORIGINAL_PRICE, DISCOUNTED_AMOUNT, 9095.0, "US", true
            );
            given(paymentService.processPayment(any(PaymentRequest.class)))
                    .willReturn(mockResult);

            // When & Then
            mockMvc.perform(post("/api/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())  // 201 Created
                    .andExpect(jsonPath("$.country").value("US"))
                    .andExpect(jsonPath("$.taxedAmount").value(9095.0));
        }
    }

    @Nested
    @DisplayName("GET /api/payments/{id} - 결제 조회")
    class GetPaymentTest {

        @Test
        @DisplayName("존재하는 결제를 ID로 조회하면 200 OK와 결제 정보를 반환한다")
        void shouldGetPaymentById() throws Exception {
            // Given
            Payment mockPayment = createMockPayment(PAYMENT_ID);
            given(paymentService.getPayment(PAYMENT_ID))
                    .willReturn(mockPayment);

            // When & Then
            mockMvc.perform(get("/api/payments/{id}", PAYMENT_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PAYMENT_ID))
                    .andExpect(jsonPath("$.originalPrice").value(ORIGINAL_PRICE))
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
        void shouldReturn404ForNonExistentPayment() throws Exception {
            // Given
            Long nonExistentId = 9999L;
            given(paymentService.getPayment(nonExistentId))
                    .willThrow(new IllegalArgumentException("결제를 찾을 수 없습니다: " + nonExistentId));

            // When & Then
            mockMvc.perform(get("/api/payments/{id}", nonExistentId))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/payments - 전체 결제 조회")
    class GetAllPaymentsTest {

        @Test
        @DisplayName("전체 결제 목록을 조회하면 200 OK와 결제 목록을 반환한다")
        void shouldGetAllPayments() throws Exception {
            // Given
            List<Payment> mockPayments = Arrays.asList(
                    createMockPayment(1L),
                    createMockPayment(2L),
                    createMockPayment(3L)
            );
            given(paymentService.getAllPayments())
                    .willReturn(mockPayments);

            // When & Then
            mockMvc.perform(get("/api/payments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[2].id").value(3));
        }

        @Test
        @DisplayName("결제가 없으면 빈 배열을 반환한다")
        void shouldReturnEmptyListWhenNoPayments() throws Exception {
            // Given
            given(paymentService.getAllPayments())
                    .willReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/payments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/payments/status?status={status} - 상태별 조회")
    class GetPaymentsByStatusTest {

        @Test
        @DisplayName("COMPLETED 상태의 결제만 조회할 수 있다")
        void shouldGetCompletedPayments() throws Exception {
            // Given
            List<Payment> completedPayments = Arrays.asList(
                    createMockPayment(1L),
                    createMockPayment(2L)
            );
            given(paymentService.getPaymentsByStatus(PaymentStatus.COMPLETED))
                    .willReturn(completedPayments);

            // When & Then - 쿼리 파라미터 방식
            mockMvc.perform(get("/api/payments/status")
                            .param("status", "COMPLETED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].status", everyItem(is("COMPLETED"))));
        }

        @Test
        @DisplayName("PENDING 상태의 결제를 조회할 수 있다")
        void shouldGetPendingPayments() throws Exception {
            // Given
            given(paymentService.getPaymentsByStatus(PaymentStatus.PENDING))
                    .willReturn(List.of());

            // When & Then - 쿼리 파라미터 방식
            mockMvc.perform(get("/api/payments/status")
                            .param("status", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("PATCH /api/payments/{id}/refund - 결제 환불")
    class RefundPaymentTest {

        @Test
        @DisplayName("완료된 결제를 환불하면 200 OK와 환불된 결제 정보를 반환한다")
        void shouldRefundPayment() throws Exception {
            // Given
            Payment refundedPayment = createMockPayment(PAYMENT_ID);
            // 리플렉션으로 상태 변경 (테스트용)
            java.lang.reflect.Field statusField = Payment.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(refundedPayment, PaymentStatus.REFUNDED);

            given(paymentService.refundPayment(PAYMENT_ID))
                    .willReturn(refundedPayment);

            // When & Then - PATCH 메서드 사용 (부분 수정)
            mockMvc.perform(patch("/api/payments/{id}/refund", PAYMENT_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PAYMENT_ID))
                    .andExpect(jsonPath("$.status").value("REFUNDED"));
        }

        @Test
        @DisplayName("존재하지 않는 결제 환불 시 예외가 발생한다")
        void shouldReturn400ForNonExistentPaymentRefund() throws Exception {
            // Given
            Long nonExistentId = 9999L;
            given(paymentService.refundPayment(nonExistentId))
                    .willThrow(new IllegalArgumentException("결제를 찾을 수 없습니다"));

            // When & Then - PATCH 메서드 사용
            mockMvc.perform(patch("/api/payments/{id}/refund", nonExistentId))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Content-Type 및 HTTP 메서드 테스트")
    class HttpMethodTest {

        @Test
        @DisplayName("POST 요청에 Content-Type이 없으면 415 에러")
        void shouldReturn415WhenNoContentType() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/payments")
                            .content("{\"price\": 10000}"))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("잘못된 HTTP 메서드 사용 시 405 에러")
        void shouldReturn405ForWrongMethod() throws Exception {
            // When & Then - GET 대신 DELETE 사용
            mockMvc.perform(delete("/api/payments"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }

    // ========================================================================
    // 테스트 헬퍼 메서드
    // ========================================================================

    /**
     * 테스트용 Mock Payment 객체 생성
     * - 리플렉션을 사용하여 ID와 상태 설정
     * - 실제 프로덕션에서는 사용하지 않음 (테스트 전용)
     */
    private Payment createMockPayment(Long id) {
        Payment payment = Payment.create(
                ORIGINAL_PRICE,
                DISCOUNTED_AMOUNT,
                TAXED_AMOUNT,
                COUNTRY,
                true
        );

        // 리플렉션으로 ID 설정 (테스트용)
        try {
            java.lang.reflect.Field idField = Payment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(payment, id);

            java.lang.reflect.Field statusField = Payment.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(payment, PaymentStatus.COMPLETED);
        } catch (Exception e) {
            throw new RuntimeException("테스트 데이터 설정 실패", e);
        }

        return payment;
    }
}
