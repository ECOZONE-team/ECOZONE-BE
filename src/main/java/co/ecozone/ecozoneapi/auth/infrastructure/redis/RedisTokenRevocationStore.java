package co.ecozone.ecozoneapi.auth.infrastructure.redis;

import co.ecozone.ecozoneapi.auth.application.port.TokenRevocationStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;


import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Redis 기반 토큰 블랙리스트 저장소
 *  - 키: {prefix}{jti}
 *  - 값: "1"
 *  - TTL: 현재시각 ~ 토큰 만료시각
 * - 만료 시각까지 TTL로 저장하여 재사용 차단
 * @since 2025-08-23
 * @author jeongdayeon
 */
@Component
@RequiredArgsConstructor
public class RedisTokenRevocationStore implements TokenRevocationStore {

    private final StringRedisTemplate redis;
    private final Clock clock;

    @Value("${app.security.logout.revocation-prefix:auth:revoked:}")
    private String prefix;

    private String key(String jti) {
        return prefix + jti;
    }

    @Override
    public void revoke(String jti, Instant expiresAt) {
        if (jti == null) return;
        Instant now = Instant.now(clock);
        Instant exp = (expiresAt != null ? expiresAt : now.plus(Duration.ofHours(1)));
        Duration ttl = Duration.between(now, exp);
        if (ttl.isNegative() || ttl.isZero()) ttl = Duration.ofSeconds(1);
        redis.opsForValue().set(key(jti), "1", ttl);
    }

    @Override
    public boolean isRevoked(String jti) {
        if (jti == null) return false;
        Boolean exists = redis.hasKey(key(jti));
        return Boolean.TRUE.equals(exists);
    }
}