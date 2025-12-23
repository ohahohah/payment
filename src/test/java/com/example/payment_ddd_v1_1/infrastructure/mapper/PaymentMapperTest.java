package com.example.payment_ddd_v1_1.infrastructure.mapper;

import com.example.payment_ddd_v1_1.domain.model.Country;
import com.example.payment_ddd_v1_1.domain.model.Money;
import com.example.payment_ddd_v1_1.domain.model.Payment;
import com.example.payment_ddd_v1_1.domain.model.PaymentStatus;
import com.example.payment_ddd_v1_1.infrastructure.persistence.PaymentJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * PaymentMapper 단위 테스트
 *
 * ============================================================================
 * [정석 DDD - Mapper 테스트]
 * ============================================================================
 *
 * Domain 객체와 JPA Entity 간의 변환이 정확히 이루어지는지 검증
 *
 * - toDomain(): PaymentJpaEntity → Payment (Domain)
 * - toEntity(): Payment (Domain) → PaymentJpaEntity
 *
 * ============================================================================
 * [왜 Mapper 테스트가 필요한가?]
 * ============================================================================
 *
 * 1. 데이터 무결성
 *    - Value Object (Money, Country)가 원시 타입으로 올바르게 변환되는지
 *    - 상태(PaymentStatus)가 String으로 올바르게 저장/복원되는지
 *
 * 2. 회귀 방지
 *    - Mapper 변경 시 기존 기능이 깨지지 않음을 보장
 */
@DisplayName("PaymentMapper 테스트")
class PaymentMapperTest {

    private PaymentMapper mapper;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @BeforeEach
    void setUp() {
        mapper = new PaymentMapper();
        createdAt = LocalDateTime.of(2024, 1, 15, 10, 30);
        updatedAt = LocalDateTime.of(2024, 1, 15, 11, 0);
    }

    @Nested
    @DisplayName("toDomain() 테스트 - JPA Entity → Domain")
    class ToDomainTest {

        @Test
        @DisplayName("JPA Entity를 Domain Payment로 변환")
        void convertToDomain() {
            // given
            PaymentJpaEntity entity = new PaymentJpaEntity();
            entity.setId(1L);
            entity.setOriginalPrice(10000.0);
            entity.setDiscountedAmount(9000.0);
            entity.setTaxedAmount(9900.0);
            entity.setCountry("KR");
            entity.setVip(false);
            entity.setStatus("PENDING");
            entity.setCreatedAt(createdAt);
            entity.setUpdatedAt(updatedAt);

            // when
            Payment payment = mapper.toDomain(entity);

            // then
            assertThat(payment.getId()).isEqualTo(1L);
            assertThat(payment.getOriginalPrice().getAmount()).isEqualTo(10000.0);
            assertThat(payment.getDiscountedAmount().getAmount()).isEqualTo(9000.0);
            assertThat(payment.getTaxedAmount().getAmount()).isEqualTo(9900.0);
            assertThat(payment.getCountry().getCode()).isEqualTo("KR");
            assertThat(payment.isVip()).isFalse();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getCreatedAt()).isEqualTo(createdAt);
            assertThat(payment.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("VIP 결제 변환")
        void convertVipPaymentToDomain() {
            // given
            PaymentJpaEntity entity = new PaymentJpaEntity();
            entity.setId(2L);
            entity.setOriginalPrice(10000.0);
            entity.setDiscountedAmount(8000.0);
            entity.setTaxedAmount(8800.0);
            entity.setCountry("KR");
            entity.setVip(true);
            entity.setStatus("COMPLETED");
            entity.setCreatedAt(createdAt);
            entity.setUpdatedAt(updatedAt);

            // when
            Payment payment = mapper.toDomain(entity);

            // then
            assertThat(payment.isVip()).isTrue();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("모든 상태 변환 테스트")
        void convertAllStatuses() {
            // given
            PaymentJpaEntity entity = createBaseEntity();

            // when & then - PENDING
            entity.setStatus("PENDING");
            assertThat(mapper.toDomain(entity).getStatus()).isEqualTo(PaymentStatus.PENDING);

            // when & then - COMPLETED
            entity.setStatus("COMPLETED");
            assertThat(mapper.toDomain(entity).getStatus()).isEqualTo(PaymentStatus.COMPLETED);

            // when & then - FAILED
            entity.setStatus("FAILED");
            assertThat(mapper.toDomain(entity).getStatus()).isEqualTo(PaymentStatus.FAILED);

            // when & then - REFUNDED
            entity.setStatus("REFUNDED");
            assertThat(mapper.toDomain(entity).getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }
    }

    @Nested
    @DisplayName("toEntity() 테스트 - Domain → JPA Entity")
    class ToEntityTest {

        @Test
        @DisplayName("Domain Payment를 JPA Entity로 변환")
        void convertToEntity() {
            // given
            Payment payment = Payment.reconstitute(
                    1L,
                    Money.of(10000),
                    Money.of(9000),
                    Money.of(9900),
                    Country.of("KR"),
                    false,
                    PaymentStatus.PENDING,
                    createdAt,
                    updatedAt
            );

            // when
            PaymentJpaEntity entity = mapper.toEntity(payment);

            // then
            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getOriginalPrice()).isEqualTo(10000.0);
            assertThat(entity.getDiscountedAmount()).isEqualTo(9000.0);
            assertThat(entity.getTaxedAmount()).isEqualTo(9900.0);
            assertThat(entity.getCountry()).isEqualTo("KR");
            assertThat(entity.getVip()).isFalse();
            assertThat(entity.getStatus()).isEqualTo("PENDING");
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("새 결제 (ID null) 변환")
        void convertNewPaymentToEntity() {
            // given
            Payment payment = Payment.create(
                    Money.of(10000),
                    Money.of(9000),
                    Money.of(9900),
                    Country.of("KR"),
                    false
            );

            // when
            PaymentJpaEntity entity = mapper.toEntity(payment);

            // then
            assertThat(entity.getId()).isNull();
            assertThat(entity.getOriginalPrice()).isEqualTo(10000.0);
            assertThat(entity.getStatus()).isEqualTo("PENDING");
        }
    }

    @Nested
    @DisplayName("왕복 변환 테스트 (Round-trip)")
    class RoundTripTest {

        @Test
        @DisplayName("Domain → Entity → Domain 왕복 변환 시 데이터 보존")
        void roundTripDomainToEntityToDomain() {
            // given
            Payment original = Payment.reconstitute(
                    1L,
                    Money.of(10000),
                    Money.of(9000),
                    Money.of(9900),
                    Country.of("KR"),
                    true,
                    PaymentStatus.COMPLETED,
                    createdAt,
                    updatedAt
            );

            // when
            PaymentJpaEntity entity = mapper.toEntity(original);
            Payment restored = mapper.toDomain(entity);

            // then
            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getOriginalPrice()).isEqualTo(original.getOriginalPrice());
            assertThat(restored.getDiscountedAmount()).isEqualTo(original.getDiscountedAmount());
            assertThat(restored.getTaxedAmount()).isEqualTo(original.getTaxedAmount());
            assertThat(restored.getCountry()).isEqualTo(original.getCountry());
            assertThat(restored.isVip()).isEqualTo(original.isVip());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
            assertThat(restored.getCreatedAt()).isEqualTo(original.getCreatedAt());
            assertThat(restored.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
        }

        @Test
        @DisplayName("Entity → Domain → Entity 왕복 변환 시 데이터 보존")
        void roundTripEntityToDomainToEntity() {
            // given
            PaymentJpaEntity original = new PaymentJpaEntity();
            original.setId(1L);
            original.setOriginalPrice(10000.0);
            original.setDiscountedAmount(9000.0);
            original.setTaxedAmount(9900.0);
            original.setCountry("KR");
            original.setVip(true);
            original.setStatus("COMPLETED");
            original.setCreatedAt(createdAt);
            original.setUpdatedAt(updatedAt);

            // when
            Payment domain = mapper.toDomain(original);
            PaymentJpaEntity restored = mapper.toEntity(domain);

            // then
            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getOriginalPrice()).isEqualTo(original.getOriginalPrice());
            assertThat(restored.getDiscountedAmount()).isEqualTo(original.getDiscountedAmount());
            assertThat(restored.getTaxedAmount()).isEqualTo(original.getTaxedAmount());
            assertThat(restored.getCountry()).isEqualTo(original.getCountry());
            assertThat(restored.getVip()).isEqualTo(original.getVip());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
            assertThat(restored.getCreatedAt()).isEqualTo(original.getCreatedAt());
            assertThat(restored.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
        }
    }

    private PaymentJpaEntity createBaseEntity() {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setId(1L);
        entity.setOriginalPrice(10000.0);
        entity.setDiscountedAmount(9000.0);
        entity.setTaxedAmount(9900.0);
        entity.setCountry("KR");
        entity.setVip(false);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        return entity;
    }
}
