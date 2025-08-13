package co.ecozone.ecozoneapi.auth.infrastructure.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 관련 설정(app.security.jwt.*)을 바인딩하는 @ConfigurationProperties 클래스
 * 시크릿, 발급자(issuer), 액세스/리프레시 TTL, 시계 오차 허용값을 포함
 * @since 2025-08-13
 * @author jeongdayeon
 */
@Data
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {
    /**
     * Base64-encoded secret (HS256에 최소 256-bit 권장)
     */
    private String base64Secret;
    private String issuer = "ecozone";
    private long accessTtlSeconds = 900;     // 15분
    private long refreshTtlSeconds = 1209600; // 14일
    private long clockSkewSeconds = 60;      // 허용 시계 오차
}