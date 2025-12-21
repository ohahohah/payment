package com.example.payment.todoFix;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResult;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.policy.discount.DefaultDiscountPolicy;
import com.example.payment.policy.discount.DiscountPolicy;
import com.example.payment.policy.tax.KoreaTaxPolicy;
import com.example.payment.policy.tax.TaxPolicy;
import com.example.payment.policy.tax.UsTaxPolicy;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ============================================================================
 * [BAD] MessyPaymentTest - 엉망진창인 테스트 코드 예시
 * ============================================================================
 *
 * [이 테스트의 문제점들]
 *
 * 1. @SpringBootTest를 불필요하게 사용
 *    - 단순 POJO 로직 테스트에도 전체 스프링 컨텍스트를 로딩
 *    - 테스트 실행 시간이 매우 느려짐 (수 초 ~ 수십 초)
 *    - DB 연결, 웹 서버 등 불필요한 리소스 낭비
 *
 * 2. 테스트 분리가 안됨
 *    - POJO 테스트, Repository 테스트, Controller 테스트가 한 파일에 섞여있음
 *    - 테스트 실패 시 원인 파악이 어려움
 *    - 테스트 간 의존성이 생길 수 있음
 *
 * 3. 테스트 이름과 구조가 불명확
 *    - 테스트 메서드명이 무엇을 테스트하는지 불분명
 *    - Given-When-Then 구조가 없음
 *    - 테스트 의도를 파악하기 어려움
 *
 * 4. 하드코딩된 값들
 *    - 매직 넘버 사용 (0.85, 0.9, 1.1 등)
 *    - 테스트 데이터가 명확하지 않음
 *
 * 5. 불필요한 의존성
 *    - MockMvc, ObjectMapper 등을 POJO 테스트에서도 로딩
 */
@SpringBootTest  // [문제점] 모든 테스트에 전체 컨텍스트 로딩 - 매우 무거움
@AutoConfigureMockMvc
class MessyPaymentTest {

    // [문제점] POJO 테스트에는 불필요한 의존성들
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    // ========================================================================
    // [문제점] POJO 로직 테스트인데 @SpringBootTest 사용
    // 이 테스트들은 스프링 컨텍스트가 전혀 필요 없음
    // ========================================================================

    @Test
    @DisplayName("할인 정책 테스트")  // [문제점] 무엇을 테스트하는지 불명확
    void testDiscount() {
        // [문제점] @SpringBootTest로 전체 컨텍스트 로딩했지만
        // 실제로는 new로 직접 생성해서 테스트함 - 의미 없는 오버헤드
        DiscountPolicy policy = new DefaultDiscountPolicy();

        // [문제점] 매직 넘버 사용, 테스트 의도 불명확
        double result = policy.apply(10000, true);
        assertThat(result).isEqualTo(8500);

        double result2 = policy.apply(10000, false);
        assertThat(result2).isEqualTo(9000);
    }

    @Test
    @DisplayName("세금 정책 테스트")  // [문제점] 여러 케이스를 한 테스트에 몰아넣음
    void testTax() {
        // [문제점] 여러 정책을 한 테스트에서 검증 - 실패 시 어디서 실패했는지 파악 어려움
        TaxPolicy koreaPolicy = new KoreaTaxPolicy();
        TaxPolicy usPolicy = new UsTaxPolicy();

        assertThat(koreaPolicy.apply(10000)).isEqualTo(11000);
        assertThat(usPolicy.apply(10000)).isEqualTo(10700);
    }

    @Test
    @DisplayName("엔티티 테스트")  // [문제점] DB 연결 없이도 되는 테스트인데 @SpringBootTest 사용
    void testEntity() {
        // [문제점] Payment 엔티티 테스트
        // 이것은 순수 자바 객체 테스트이므로 스프링이 필요 없음
        Payment payment = Payment.create(10000.0, 8500.0, 9350.0, "KR", true);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        // setter로 상태 변경
        payment.setStatus(PaymentStatus.COMPLETED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

        payment.setStatus(PaymentStatus.REFUNDED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    // ========================================================================
    // [문제점] Repository 테스트가 다른 테스트들과 섞여있음
    // @DataJpaTest를 사용하면 더 가볍게 테스트 가능
    // ========================================================================

    @Test
    @DisplayName("리포지토리 저장 테스트")
    void testRepository() {
        // [문제점] Repository 테스트인데 @SpringBootTest 사용
        // @DataJpaTest를 사용하면 JPA 관련 빈만 로딩해서 더 빠름
        Payment payment = Payment.create(10000.0, 8500.0, 9350.0, "KR", true);
        payment.setStatus(PaymentStatus.COMPLETED);

        Payment saved = paymentRepository.save(payment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("리포지토리 조회 테스트")
    void testRepositoryFind() {
        // [문제점] 테스트 간 데이터 의존성 - 이전 테스트 데이터에 영향받을 수 있음
        Payment payment = Payment.create(20000.0, 17000.0, 18700.0, "US", false);
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        var found = paymentRepository.findByStatus(PaymentStatus.COMPLETED);

        // [문제점] 다른 테스트에서 저장한 데이터도 포함될 수 있음
        assertThat(found).isNotEmpty();
    }

    // ========================================================================
    // [문제점] Controller 테스트도 같은 파일에 섞여있음
    // @WebMvcTest를 사용하면 웹 레이어만 테스트 가능
    // ========================================================================

    @Test
    @DisplayName("API 테스트")  // [문제점] 너무 포괄적인 이름
    void testApi() throws Exception {
        // [문제점] Controller 테스트인데 @SpringBootTest + @AutoConfigureMockMvc 사용
        // @WebMvcTest를 사용하면 Controller 관련 빈만 로딩해서 더 빠름
        PaymentRequest request = new PaymentRequest(10000, "KR", true);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalPrice").value(10000))
                .andExpect(jsonPath("$.discountedAmount").value(8500));
    }

    @Test
    @DisplayName("API 조회 테스트")
    void testApiGet() throws Exception {
        // [문제점] 테스트 데이터 준비를 위해 실제 저장 - 테스트 격리 안됨
        PaymentRequest request = new PaymentRequest(15000, "KR", false);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // [문제점] 전체 목록 조회 - 다른 테스트 데이터도 포함됨
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk());
    }

    // ========================================================================
    // [문제점] Service 통합 테스트도 섞여있음
    // ========================================================================

    @Test
    @DisplayName("서비스 테스트")
    void testService() {
        // [문제점] Service 통합 테스트
        PaymentRequest request = new PaymentRequest(30000, "KR", true);

        PaymentResult result = paymentService.processPayment(request);

        assertThat(result.originalPrice()).isEqualTo(30000);
        // [문제점] 하드코딩된 계산값 - 할인율/세율 변경 시 테스트 실패
        assertThat(result.discountedAmount()).isEqualTo(25500);  // 30000 * 0.85
        assertThat(result.taxedAmount()).isEqualTo(28050);       // 25500 * 1.1 (Math.round 적용)
    }
}
