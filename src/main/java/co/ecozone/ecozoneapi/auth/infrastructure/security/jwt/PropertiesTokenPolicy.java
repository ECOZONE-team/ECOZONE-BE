package co.ecozone.ecozoneapi.auth.infrastructure.security.jwt;

import co.ecozone.ecozoneapi.auth.application.port.TokenPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 토큰 수명/시계 오차 정책 구현체
 * - 인프라 설정(JwtProperties)을 래핑하여 애플리케이션 계층에 노출
 * @since 2025-08-23
 * @author jeongdayeon 
 */
@Component
@RequiredArgsConstructor
public class PropertiesTokenPolicy implements TokenPolicy {

    private final JwtProperties props;

    @Override
    public long accessTtlSeconds() {
        return props.getAccessTtlSeconds();
    }

    @Override
    public long refreshTtlSeconds() {
        return props.getRefreshTtlSeconds();
    }

    @Override
    public long clockSkewSeconds() {
        return props.getClockSkewSeconds();
    }
}