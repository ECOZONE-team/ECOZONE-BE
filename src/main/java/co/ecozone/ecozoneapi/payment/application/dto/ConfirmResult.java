package co.ecozone.ecozoneapi.payment.application.dto;

import java.time.Instant;

/**
 * PG 승인 결과 모델 (Provider 계층 반환 타입)
 * - 외부 PG 응답(성공/실패/사유/시각)을 내부 표준 형태로 변환해 전달
 * - 애플리케이션 서비스는 이 타입만 의존
 * @since 2025-09-16
 */
public record ConfirmResult(boolean ok, Instant approvedAt, String failureReason) {}

