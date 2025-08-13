package co.ecozone.ecozoneapi.auth.domain.model;

import co.ecozone.ecozoneapi.auth.domain.vo.AccessRule;

import java.util.List;

/**
 * 접근 규칙(AccessRule) 목록을 보유하는 도메인 집합체(불변)
 * 보안 어댑터(SecurityConfig)가 이 정책을 순회하며 인가 규칙을 적용
 * @since 2025-08-13
 * @author jeongdayeon
 */
public final class SecurityPolicy {
    private final List<AccessRule> rules;
    public SecurityPolicy(List<AccessRule> rules){ this.rules = List.copyOf(rules); }
    public List<AccessRule> rules(){ return rules; }
}