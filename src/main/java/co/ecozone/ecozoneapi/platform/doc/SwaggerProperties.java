package co.ecozone.ecozoneapi.platform.doc;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger(OpenAPI) 구성 프로퍼티 바인딩
 * - app.doc.swagger.* 값을 바인딩하여 UI 활성화/경로/제목/보안스킴 등을 외부화
 * - 문서 계층은 인프라 성격이므로 infrastructure/doc 에 배치
 * @since 2025-08-23
 * @author jeongdayeon
 */
@Data
@Validated
@ConfigurationProperties(prefix = "app.swagger")
public class SwaggerProperties {

    /** 문서 활성화 여부 (필요 시 비활성화) */
    private boolean enabled = true;

    /** 문서 그룹명 (springdoc GroupedOpenApi) */
    @NotBlank
    private String group = "public";

    /** 문서화할 경로 (기본 /api/** 만 노출) */
    private List<String> pathsToMatch = new ArrayList<>(List.of("/api/**"));

    /** 문서에서 제외할 경로 */
    private List<String> pathsToExclude = new ArrayList<>(List.of("/error", "/actuator/**"));

    /** JWT 등 보안 스킴명 (Swagger UI Authorize 버튼 이름) */
    @NotBlank
    private String securitySchemeName = "jwtAuth";

    /** 보안 적용을 제외할 경로 (ex. 로그인/회원가입 등) */
    private List<String> securityExcludePaths = new ArrayList<>(List.of(
            "/api/auth/login", "/api/auth/signup"
    ));

    /** 메타 정보 */
    @NotBlank
    private String title = "Ecozone API";
    @NotBlank
    private String version = "v1";
    private String description = "Ecozone HTTP API";
    /** UI 상단 Servers 섹션 (선택) */
    private List<String> servers = new ArrayList<>();
}
