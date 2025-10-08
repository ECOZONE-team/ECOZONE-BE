package co.ecozone.ecozoneapi.auth.infrastructure.security;

import co.ecozone.ecozoneapi.auth.application.port.TokenRevocationStore;
import co.ecozone.ecozoneapi.auth.infrastructure.security.policy.CorsPolicyFactory;
import co.ecozone.ecozoneapi.auth.infrastructure.security.policy.SecurityPolicyFactory;
import co.ecozone.ecozoneapi.auth.domain.model.security.http.CorsPolicy;
import co.ecozone.ecozoneapi.auth.domain.model.security.http.CorsRule;
import co.ecozone.ecozoneapi.auth.domain.model.security.http.HttpMethodType;
import co.ecozone.ecozoneapi.auth.domain.model.security.SecurityPolicy;
import co.ecozone.ecozoneapi.auth.domain.port.out.TokenProvider;
import co.ecozone.ecozoneapi.auth.domain.model.security.http.AccessRule;
import co.ecozone.ecozoneapi.auth.domain.model.security.http.EndPointPattern;
import co.ecozone.ecozoneapi.auth.infrastructure.security.filter.JwtAuthenticationFilter;
import co.ecozone.ecozoneapi.auth.infrastructure.security.jwt.JwtProperties;
import co.ecozone.ecozoneapi.platform.web.error.JsonAccessDeniedHandler;
import co.ecozone.ecozoneapi.platform.web.error.JsonAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;


/**
 * Spring Security 어댑터 구성 클래스
 * - SecurityPolicyFactory/CorsPolicyFactory 결과를 적용
 * - JWT 필터 연결 및 세션 무상태 설정
 * - 인증 실패/인가 거부 핸들러 등록
 * 하드코딩된 경로/헤더 없이 정책/설정 기반으로 동작
 * @since 2025-08-13
 * @author jeongdayeon
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties({
        SecurityRouteProperties.class,
        CorsProperties.class,
        JwtProperties.class
})
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final TokenRevocationStore revocationStore;
    private final SecurityPolicyFactory policyFactory;
    private final CorsPolicyFactory corsPolicyFactory;
    private final ObjectMapper om;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, revocationStore);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 1. 완전 무시(ignoring)는 정적 리소스 등 최소한으로만
        return web -> web.ignoring().requestMatchers(
                policyFactory.ignoredPatterns().toArray(String[]::new)
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SecurityPolicy policy = policyFactory.create();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(reg -> {
                    reg.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    // 1. 도메인 정책을 순회하며 적용
                    for (AccessRule rule : policy.rules()) {
                        var patterns = rule.patterns().stream().map(EndPointPattern::value).toArray(String[]::new);
                        switch (rule.level()) {
                            case PERMIT_ALL -> {
                                if (rule.method() == HttpMethodType.ALL) {
                                    reg.requestMatchers(patterns).permitAll();
                                } else {
                                    reg.requestMatchers(convert(rule.method()), patterns).permitAll();
                                }
                            }
                            case AUTHENTICATED -> {
                                if (rule.method() == HttpMethodType.ALL) {
                                    reg.requestMatchers(patterns).authenticated();
                                } else {
                                    reg.requestMatchers(convert(rule.method()), patterns).authenticated();
                                }
                            }
                            case ROLE_BASED -> reg.requestMatchers(patterns)
                                    .hasAnyRole(rule.roles().toArray(String[]::new));
                        }
                    }
                    // 2. 남는 모든 건 차단 (화이트리스트 방식)
                    reg.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(h -> h
                        .authenticationEntryPoint(new JsonAuthenticationEntryPoint(om))
                        .accessDeniedHandler(new JsonAccessDeniedHandler(om))
                );
        ;

        return http.build();
    }

    private static HttpMethod convert(HttpMethodType t) {
        return switch (t) {
            case GET -> HttpMethod.GET;
            case POST -> HttpMethod.POST;
            case PUT -> HttpMethod.PUT;
            case PATCH -> HttpMethod.PATCH;
            case DELETE -> HttpMethod.DELETE;
            case OPTIONS -> HttpMethod.OPTIONS;
            case ALL -> null;
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsPolicy policy = corsPolicyFactory.create();

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        for (CorsRule r : policy.rules()) {
            CorsConfiguration cfg = new CorsConfiguration();

            // allowCredentials + "*" 조합 주의: Spring이 막음 → patterns 사용
            if (r.allowCredentials() && r.allowedOrigins().stream().anyMatch("*"::equals)) {
                // 강제 변환: "*"는 패턴으로 옮기고 origins는 비움
                cfg.setAllowedOrigins(null);
                if (r.allowedOriginPatterns().isEmpty()) {
                    cfg.setAllowedOriginPatterns(java.util.List.of("*"));
                } else {
                    cfg.setAllowedOriginPatterns(r.allowedOriginPatterns());
                }
            } else {
                if (!r.allowedOrigins().isEmpty()) cfg.setAllowedOrigins(r.allowedOrigins());
                if (!r.allowedOriginPatterns().isEmpty()) cfg.setAllowedOriginPatterns(r.allowedOriginPatterns());
            }

            if (!r.allowedMethods().isEmpty()) cfg.setAllowedMethods(r.allowedMethods());
            if (!r.allowedHeaders().isEmpty()) cfg.setAllowedHeaders(r.allowedHeaders());
            if (!r.exposedHeaders().isEmpty()) cfg.setExposedHeaders(r.exposedHeaders());

            cfg.setAllowCredentials(r.allowCredentials());
            cfg.setMaxAge(r.maxAgeSeconds());

            source.registerCorsConfiguration(r.path(), cfg);
        }
        return source;
    }
}
