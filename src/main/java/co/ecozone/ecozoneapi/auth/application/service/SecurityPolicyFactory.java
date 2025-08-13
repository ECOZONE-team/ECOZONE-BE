package co.ecozone.ecozoneapi.auth.application.service;

import co.ecozone.ecozoneapi.auth.domain.model.HttpMethodType;
import co.ecozone.ecozoneapi.auth.domain.model.SecurityPolicy;
import co.ecozone.ecozoneapi.auth.domain.vo.AccessRule;
import co.ecozone.ecozoneapi.auth.infrastructure.security.SecurityRouteProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * app.security.routes.* 설정을 읽어 SecurityPolicy(AccessRule 목록)로 구성하는 팩토리
 * 메서드별/역할기반 규칙을 VO로 캡슐화하여 인프라 코드의 문자열 의존 제거
 * 계층: Auth(Security) 컨텍스트 / Application 레이어
 * @since 2025-08-13
 * @author jeongdayeon
 */
@Service
@RequiredArgsConstructor
public class SecurityPolicyFactory {

    private final SecurityRouteProperties props;

    public SecurityPolicy create() {
        List<AccessRule> rules = new ArrayList<>();

        // ignore는 별도로 받고, 나머지 규칙 구성
        if (!props.getPermitAll().isEmpty()) {
            rules.add(AccessRule.permitAll(HttpMethodType.ALL, props.getPermitAll()));
        }
        if (!props.getOpenGet().isEmpty()) {
            rules.add(AccessRule.permitAll(HttpMethodType.GET, props.getOpenGet()));
        }
        for (SecurityRouteProperties.AuthRule r : props.getAuthenticated()) {
            HttpMethodType m = (r.getMethod()==null) ? HttpMethodType.ALL : HttpMethodType.valueOf(r.getMethod());
            rules.add(AccessRule.authenticated(m, r.getPatterns()));
        }
        for (SecurityRouteProperties.RoleRule r : props.getRoleBased()) {
            rules.add(AccessRule.roleBased(r.getRoles(), r.getPatterns()));
        }
        return new SecurityPolicy(rules);
    }

    public List<String> ignoredPatterns() {
        return props.getIgnore();
    }
}
