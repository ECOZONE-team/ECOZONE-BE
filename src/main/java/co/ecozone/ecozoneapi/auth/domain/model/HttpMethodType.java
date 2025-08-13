package co.ecozone.ecozoneapi.auth.domain.model;

/**
 * 정책 모델에서 사용할 HTTP 메서드 값 타입 추상화
 * Spring의 HttpMethod와 분리하여 도메인 독립성 유지 목적
 * @since 2025-08-13
 * @author jeongdayeon
 */
public enum HttpMethodType {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    OPTIONS,
    ALL
}

