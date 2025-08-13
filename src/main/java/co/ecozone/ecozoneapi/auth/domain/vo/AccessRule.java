package co.ecozone.ecozoneapi.auth.domain.vo;


import co.ecozone.ecozoneapi.auth.domain.model.AccessLevel;
import co.ecozone.ecozoneapi.auth.domain.model.HttpMethodType;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 단일 접근 규칙을 표현하는 값 객체
 * HttpMethodType, EndpointPattern, AccessLevel, roles를 캡슐화하며 불변으로 유지
 * @since 2025-08-13
 * @author jeongdayeon
 */
@AllArgsConstructor
public final class AccessRule {
    private final HttpMethodType method;
    private final Set<EndPointPattern> patterns;
    private final AccessLevel level;
    private final Set<String> roles;

    private AccessRule(HttpMethodType method, Collection<String> patterns, AccessLevel level, Collection<String> roles){
        this.method = method == null ? HttpMethodType.ALL : method;
        this.patterns = patterns.stream().map(EndPointPattern::new).collect(Collectors.toUnmodifiableSet());
        this.level = level;
        this.roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    public static AccessRule permitAll(HttpMethodType m, Collection<String> p) {
        return new AccessRule(m, p, AccessLevel.PERMIT_ALL, List.of());
    }
    public static AccessRule authenticated(HttpMethodType m, Collection<String> p) {
        return new AccessRule(m, p, AccessLevel.AUTHENTICATED, List.of());
    }
    public static AccessRule roleBased(Collection<String> roles, Collection<String> p) {
        return new AccessRule(HttpMethodType.ALL, p, AccessLevel.ROLE_BASED, roles);
    }
    public HttpMethodType method(){ return method; }
    public Set<EndPointPattern> patterns(){ return patterns; }
    public AccessLevel level(){ return level; }
    public Set<String> roles(){ return roles; }
}
