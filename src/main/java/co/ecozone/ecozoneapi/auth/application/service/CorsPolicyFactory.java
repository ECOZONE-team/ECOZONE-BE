package co.ecozone.ecozoneapi.auth.application.service;

import co.ecozone.ecozoneapi.auth.domain.model.CorsPolicy;
import co.ecozone.ecozoneapi.auth.domain.model.CorsRule;
import co.ecozone.ecozoneapi.auth.infrastructure.security.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 외부 설정(app.security.cors.*)을 도메인 CORS 정책(CorsPolicy/CorsRule)으로 변환하는 팩토리
 * 계층: Auth(Security) 컨텍스트 / Application 레이어
 * 목적: 하드코딩 제거, 환경별 정책 교체 용이, SecurityConfig는 적용만 담당.
 * Thread-safety: 무상태(Stateless)
 * @since 2025-08-13
 * @author jeongdayeon
 */
@Service
@RequiredArgsConstructor
public class CorsPolicyFactory {

    private final CorsProperties props;

    public CorsPolicy create() {
        List<CorsRule> rules = props.getMappings().stream()
                .map(m -> new CorsRule(
                        m.getPath(),
                        m.getAllowedOrigins(),
                        m.getAllowedOriginPatterns(),
                        m.getAllowedMethods(),
                        m.getAllowedHeaders(),
                        m.getExposedHeaders(),
                        Boolean.TRUE.equals(m.getAllowCredentials()),
                        m.getMaxAge() == null ? 1800L : m.getMaxAge()
                ))
                .toList();
        return new CorsPolicy(rules);
    }
}
