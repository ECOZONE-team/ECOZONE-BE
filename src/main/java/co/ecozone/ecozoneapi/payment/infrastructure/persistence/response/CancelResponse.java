package co.ecozone.ecozoneapi.payment.infrastructure.persistence.response;

/**
 * 결제 취소 응답 DTO (Web 계층)
 * - Controller → 클라이언트 응답 모델
 * - 도메인 개체를 외부로 직접 노출하지 않도록 캡슐화
 * @since 2025-09-16
 */

public record CancelResponse(Long paymentId) {}