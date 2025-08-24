package co.ecozone.ecozoneapi.auth.application.port;

import co.ecozone.ecozoneapi.auth.domain.model.UserId;

import java.time.Instant;

/**
 * Refresh 토큰 관리 포트
 * - 현재 유효한 Refresh JTI 등록/검증/무효화
 * - 회전(rotations) 및 재사용(reuse) 공격 방지용
 * @since 2025-08-23
 */

public interface RefreshTokenRegistry {
    void setCurrent(UserId userId, String refreshJti, Instant expiresAt);
    boolean isCurrent(UserId userId, String refreshJti);
    void invalidate(UserId userId); // 로그인/로그아웃 시 초기화
}