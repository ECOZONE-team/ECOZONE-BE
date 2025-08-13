package co.ecozone.ecozoneapi.auth.domain.model;

import java.util.List;

/**
 * 경로 단위 CORS 규칙(CorsRule)을 보유하는 도메인 집합체
 * 프레임워크 비침투적
 * SecurityConfig에서 Adapter가 적용
 * @since 2025-08-13
 * @author jeongdayeon
 */
public record CorsPolicy(List<CorsRule> rules) { }

