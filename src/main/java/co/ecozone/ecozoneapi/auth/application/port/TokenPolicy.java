package co.ecozone.ecozoneapi.auth.application.port;

/**
 * 토큰 수명/시계 오차 정책 포트
 * - 인프라 설정(JwtProperties)을 숨기고 애플리케이션에 추상화 제공
 * @since 2025-08-23
 */
public interface TokenPolicy {
    long accessTtlSeconds();
    long refreshTtlSeconds();
    long clockSkewSeconds();
}
