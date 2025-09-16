package co.ecozone.ecozoneapi.payment.application.dto;

import java.time.Instant;

/**
 * PG 취소 결과 모델 (Provider 계층 반환 타입)
 * - 외부 PG 통신 결과를 애플리케이션에서 해석하기 위한 최소 정보
 * - 성공 여부/취소시각/에러 메시지 등 보유
 * @since 2025-09-16
 */
public record CancelResult(boolean ok, Instant canceledAt, String failureReason) {}
