package co.ecozone.ecozoneapi.auth.domain.model;

import java.time.Instant;
import java.util.Set;

/**
 * 토큰 검증 성공을 표현하는 도메인 결과 타입
 * 사용자 식별자, 역할, 발급/만료시각, 발급자/토큰ID를 보유
 * @since 2025-08-13
 * @author jeongdayeon
 */
public record TokenSuccess(
        UserId userId, Set<Role> roles,
        Instant issuedAt, Instant expiresAt,
        String issuer, String tokenId, String raw
) implements TokenVerification { @Override public boolean valid() { return true; } }
