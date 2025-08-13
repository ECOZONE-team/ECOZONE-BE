package co.ecozone.ecozoneapi.auth.domain.model;

import java.util.List;

/**
 * 단일 경로에 바인딩되는 CORS 규칙 값 객체
 * app.security.cors.mappings[*] 구조를 1:1로 표현
 * @since 2025-08-13
 * @author jeongdayeon
 */
public record CorsRule(
        String path,
        List<String> allowedOrigins,
        List<String> allowedOriginPatterns,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        List<String> exposedHeaders,
        boolean allowCredentials,
        long maxAgeSeconds
) { }