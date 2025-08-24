package co.ecozone.ecozoneapi.auth.infrastructure.redis;

import co.ecozone.ecozoneapi.auth.application.port.RefreshTokenRegistry;
import co.ecozone.ecozoneapi.auth.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Refresh 토큰 레지스트리의 Redis 구현
 * - 사용자별 현재 유효한 Refresh JTI 저장
 * - 회전 시 새 JTI로 교체, 만료 시 TTL로 자동 삭제
 * @since 2025-08-23
 * @author jeongdayeon
 */
@Component
@RequiredArgsConstructor
public class RedisRefreshTokenRegistry implements RefreshTokenRegistry {

    private final StringRedisTemplate redis;
    private final Clock clock;

    @Value("${app.security.refresh.current-prefix:auth:refresh:current:}")
    private String prefix;

    private String key(UserId id) { return prefix + id.value(); }

    @Override
    public void setCurrent(UserId userId, String jti, Instant exp) {
        Instant now = Instant.now(clock);
        Duration ttl = Duration.between(now, exp);
        if (ttl.isNegative() || ttl.isZero()) ttl = Duration.ofSeconds(1);
        redis.opsForValue().set(key(userId), jti, ttl);
    }

    @Override
    public boolean isCurrent(UserId userId, String jti) {
        String v = redis.opsForValue().get(key(userId));
        return v != null && v.equals(jti);
    }

    @Override
    public void invalidate(UserId userId) {
        redis.delete(key(userId));
    }
}