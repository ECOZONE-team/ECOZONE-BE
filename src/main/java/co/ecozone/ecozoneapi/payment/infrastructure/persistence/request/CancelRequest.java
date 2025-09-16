package co.ecozone.ecozoneapi.payment.infrastructure.persistence.request;

/**
 * 결제 취소 요청 DTO (Web 계층)
 * - Controller ← 클라이언트 바디 매핑
 * - 단순 요청 데이터 보유, 비즈니스 규칙 없음
 * @since 2025-09-16
 */
public record CancelRequest(String reason) {}
