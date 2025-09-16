package co.ecozone.ecozoneapi.payment.infrastructure.persistence.response;

/**
 * 결제 승인 응답 DTO (Web 계층)
 * - 승인된 결제 식별자/상태/승인시각 등 최소 정보 제공
 * - API 계약의 안정성을 위해 도메인 분리
 * @since 2025-09-16
 */
public record ConfirmResponse(Long paymentId) {}
