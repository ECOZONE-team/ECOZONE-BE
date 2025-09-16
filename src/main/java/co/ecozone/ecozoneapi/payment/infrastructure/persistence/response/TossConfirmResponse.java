package co.ecozone.ecozoneapi.payment.infrastructure.persistence.response;

import java.time.Instant;

/**
 * Toss 승인 응답 DTO (인프라)
 * - 외부 API 응답을 내부 모델로 역직렬화하기 위한 전용 타입
 * - Provider에서만 사용, 외부로 노출 금지
 * @since 2025-09-16
 */
public record TossConfirmResponse(Instant approvedAt) {}
