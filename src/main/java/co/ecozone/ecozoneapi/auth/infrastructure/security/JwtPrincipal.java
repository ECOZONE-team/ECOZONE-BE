package co.ecozone.ecozoneapi.auth.infrastructure.security;

import co.ecozone.ecozoneapi.auth.domain.model.security.Role;
import co.ecozone.ecozoneapi.auth.domain.model.UserId;

import java.time.Instant;
import java.util.Set;

/**
 * 웹 어댑터 전용 인증 주체.
 * - userId/roles/issuedAt/expiresAt/jti/issuer 등 토큰 컨텍스트를 캡슐화
 * - 컨트롤러 계층에서만 사용, 도메인/유스케이스로 누수 금지
 * @since 2025-08-23
 * @author jeongdayeon
 */
public record JwtPrincipal(
        UserId userId,
        Set<Role> roles,
        Instant issuedAt,
        Instant expiresAt,
        String jti,
        String issuer
) {}