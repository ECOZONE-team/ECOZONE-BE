package co.ecozone.ecozoneapi.platform.doc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * Swagger/OpenAPI 설정
 * - 보안 스킴/경로/메타 정보를 프로퍼티에서 로드
 * - /api/** 만 문서화하고 내부 경로는 제외
 * @since 2025-08-23
 * @author jeongdayeon
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SwaggerProperties.class)
public class SwaggerConfig {

    private final SwaggerProperties props;

    /** OpenAPI 메타 + 전역 보안 스킴(베어러) 정의 */
    @Bean
    public OpenAPI openAPI() {
        if (!props.isEnabled()) {
            log.info("Swagger/OpenAPI is disabled by configuration.");
            return new OpenAPI();
        }

        String scheme = props.getSecuritySchemeName();
        var components = new Components()
                .addSecuritySchemes(scheme, new SecurityScheme()
                        .name(scheme)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        var api = new OpenAPI()
                .components(components)
                .info(new Info()
                        .title(props.getTitle())
                        .version(props.getVersion())
                        .description(props.getDescription()))
                .addSecurityItem(new SecurityRequirement().addList(scheme));

        // Servers 섹션(선택)
        for (String s : props.getServers()) {
            api.addServersItem(new Server().url(s));
        }
        return api;
    }

    /** 문서에 실릴 엔드포인트 그룹(/api/**)과 커스터마이저 등록 */
    @Bean
    public GroupedOpenApi publicApi(OpenApiCustomizer securityCustomizer) {
        if (!props.isEnabled()) {
            // disabled라도 빈은 반환해야 NPE 없음
            return GroupedOpenApi.builder().group("disabled").pathsToMatch("/__disabled__").build();
        }

        var b = GroupedOpenApi.builder()
                .group(props.getGroup())
                .addOpenApiCustomizer(securityCustomizer);

        if (!props.getPathsToMatch().isEmpty()) {
            b.pathsToMatch(props.getPathsToMatch().toArray(String[]::new));
        }
        if (!props.getPathsToExclude().isEmpty()) {
            b.pathsToExclude(props.getPathsToExclude().toArray(String[]::new));
        }
        return b.build();
    }

    /**
     * 보안 스킴 전역 적용하되, 보안 제외 경로는 operation 단위에서 security 제거
     *  - 상단 OpenAPI에 전역 SecurityRequirement를 넣고
     *  - 제외 경로는 operation.setSecurity(List.of())로 재정의
     */
    @Bean
    public OpenApiCustomizer securityCustomizer() {
        final String scheme = props.getSecuritySchemeName();
        final var matcher = new AntPathMatcher();
        final List<String> exclude = props.getSecurityExcludePaths();

        return openApi -> {
            if (openApi.getPaths() == null) return;
            openApi.getPaths().forEach((path, item) -> {
                boolean excluded = exclude.stream().anyMatch(p -> matcher.match(p, path));
                if (item.readOperations() == null) return;
                item.readOperations().forEach(op -> {
                    if (excluded) {
                        // 이 오퍼레이션은 보안 예외: 개별 보안 요구사항 제거
                        op.setSecurity(List.of());
                    }
                });
            });
        };
    }
}