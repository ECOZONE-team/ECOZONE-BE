package co.ecozone.ecozoneapi.payment.infrastructure.persistence.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 결제 승인 요청 DTO (Web 계층)
 * - paymentKey/orderId/amount 입력값 검증 어노테이션과 함께 사용
 * - Controller 바운더리에서만 사용
 * @since 2025-09-16
 */
public record ConfirmRequest(
        @NotBlank String paymentKey,
        @NotBlank String orderId,
        @Min(1) long amount) {}
