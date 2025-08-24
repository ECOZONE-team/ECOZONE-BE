package co.ecozone.ecozoneapi.auth.application.command;

import co.ecozone.ecozoneapi.auth.domain.model.UserId;

import java.time.Instant;

/**
 * 로그아웃 유스케이스 입력 모델
 * - 현재 사용자의 UserId, 사용 중인 액세스 토큰의 jti/만료시각 전달
 * - 인프라 타입(JwtPrincipal) 대신 도메인 값만 노출
 * @since 2025-08-23
 */
public record LogoutCommand(UserId userId, String jti, Instant expiresAt) {}

