package co.ecozone.ecozoneapi.auth.infrastructure.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 라우팅 접근 정책(app.security.routes.*)을 바인딩하는 @ConfigurationProperties
 * ignore/permitAll/openGet/authenticated/roleBased를 포함하며
 * SecurityPolicyFactory가 이를 해석해 도메인 정책을 생성
 * @since 2025-08-13
 * @author jeongdayeon
 */
@ConfigurationProperties(prefix = "app.security.routes")
@Data
public class SecurityRouteProperties {

    private List<String> ignore = List.of();
    private List<String> permitAll = List.of();
    private List<String> openGet = List.of();
    private List<AuthRule> authenticated = List.of();
    private List<RoleRule> roleBased = List.of();

    @Data
    public static class AuthRule {
        private String method;
        private List<String> patterns = List.of();
    }

    @Data
    public static class RoleRule {
        private List<String> roles = List.of();
        private List<String> patterns = List.of();
    }
}
