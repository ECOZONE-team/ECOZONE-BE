package co.ecozone.ecozoneapi.auth.infrastructure.security;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import jakarta.validation.Valid;

import java.util.List;

/**
 * CORS 매핑(app.security.cors.mappings[*])을 바인딩하는 @ConfigurationProperties
 * jakarta.validation으로 기본 검증을 수행하며, CorsPolicyFactory가 사용
 * @since 2025-08-13
 * @author jeongdayeon
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.cors")
public class CorsProperties {

    @Valid
    private List<Mapping> mappings = List.of();

    @Data
    public static class Mapping {
        @NotBlank
        private String path;
        private List<String> allowedOrigins = List.of();
        private List<String> allowedOriginPatterns = List.of();
        private List<String> allowedMethods = List.of();
        private List<String> allowedHeaders = List.of();
        private List<String> exposedHeaders = List.of();
        private Boolean allowCredentials = Boolean.FALSE;
        private Long maxAge = 1800L;
    }
}
