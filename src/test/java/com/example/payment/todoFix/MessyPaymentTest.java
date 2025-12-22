package com.example.payment.todoFix;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResult;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.strategy.discount.DefaultDiscountStrategy;
import com.example.payment.strategy.discount.DiscountStrategy;
import com.example.payment.strategy.tax.KoreaTaxStrategy;
import com.example.payment.strategy.tax.TaxStrategy;
import com.example.payment.strategy.tax.UsTaxStrategy;
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
 * [BAD] MessyPaymentTest - 엉망진창인 테스트 코드 예시
 */
@SpringBootTest
@AutoConfigureMockMvc
class MessyPaymentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    @Test
    @DisplayName("할인 전략 테스트")
    void testDiscount() {
        DiscountStrategy strategy = new DefaultDiscountStrategy();
        double result = strategy.apply(10000, true);
        assertThat(result).isEqualTo(8500);

        double result2 = strategy.apply(10000, false);
        assertThat(result2).isEqualTo(9000);
    }

    @Test
    @DisplayName("세금 전략 테스트")
    void testTax() {
        TaxStrategy koreaStrategy = new KoreaTaxStrategy();
        TaxStrategy usStrategy = new UsTaxStrategy();

        assertThat(koreaStrategy.apply(10000)).isEqualTo(11000);
        assertThat(usStrategy.apply(10000)).isEqualTo(10700);
    }

    @Test
    @DisplayName("엔티티 테스트")
    void testEntity() {
        Payment payment = Payment.create(10000.0, 8500.0, 9350.0, "KR", true);

        assertThat(payment.getStat()).isEqualTo(PaymentStatus.P);

        payment.setStat(PaymentStatus.C);
        assertThat(payment.getStat()).isEqualTo(PaymentStatus.C);

        payment.setStat(PaymentStatus.R);
        assertThat(payment.getStat()).isEqualTo(PaymentStatus.R);
    }

    @Test
    @DisplayName("리포지토리 저장 테스트")
    void testRepository() {
        Payment payment = Payment.create(10000.0, 8500.0, 9350.0, "KR", true);
        payment.setStat(PaymentStatus.C);

        Payment saved = paymentRepository.save(payment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStat()).isEqualTo(PaymentStatus.C);
    }

    @Test
    @DisplayName("리포지토리 조회 테스트")
    void testRepositoryFind() {
        Payment payment = Payment.create(20000.0, 17000.0, 18700.0, "US", false);
        payment.setStat(PaymentStatus.C);
        paymentRepository.save(payment);

        var found = paymentRepository.findByStat(PaymentStatus.C);

        assertThat(found).isNotEmpty();
    }

    @Test
    @DisplayName("API 테스트")
    void testApi() throws Exception {
        PaymentRequest request = new PaymentRequest(10000, "KR", true);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amt1").value(10000))
                .andExpect(jsonPath("$.amt2").value(8500));
    }

    @Test
    @DisplayName("API 조회 테스트")
    void testApiGet() throws Exception {
        PaymentRequest request = new PaymentRequest(15000, "KR", false);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("서비스 테스트")
    void testService() {
        PaymentRequest request = new PaymentRequest(30000, "KR", true);

        PaymentResult result = paymentService.execute(request);

        assertThat(result.amt1()).isEqualTo(30000);
        assertThat(result.amt2()).isEqualTo(25500);
        assertThat(result.amt3()).isEqualTo(28050);
    }
}
