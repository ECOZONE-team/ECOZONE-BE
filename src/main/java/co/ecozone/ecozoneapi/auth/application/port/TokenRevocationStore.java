package co.ecozone.ecozoneapi.auth.application.port;

import java.time.Instant;

/**
 * 액세스 토큰 블랙리스트 저장소 포트
 * - 로그아웃 시 JTI를 만료까지 저장(TTL)하고, 요청마다 무효 여부를 조회
 * @since 2025-08-23
 */
public interface TokenRevocationStore {
    void revoke(String jti, Instant expiresAt);
    boolean isRevoked(String jti);
}