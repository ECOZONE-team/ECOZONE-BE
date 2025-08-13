package co.ecozone.ecozoneapi.auth.domain.port.out;

import co.ecozone.ecozoneapi.auth.domain.model.Role;
import co.ecozone.ecozoneapi.auth.domain.model.TokenVerification;
import co.ecozone.ecozoneapi.auth.domain.model.UserId;

import java.util.Set;

/**
 * 토큰 발급·검증을 위한 도메인 아웃바운드 포트
 * 인프라(JWT 등) 구현체가 이 인터페이스를 구현하며, 코어는 포트에만 의존
 * @since 2025-08-13
 */
public interface TokenProvider {
    String generateAccessToken(UserId userId, Set<Role> roles);
    String generateRefreshToken(UserId userId);
    TokenVerification verify(String jwt);
}
