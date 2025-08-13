package co.ecozone.ecozoneapi.auth.domain.model;

/**
 * 도메인 역할(USER, ADMIN 등)을 나타내는 열거형
 * Spring Security 호환을 위해 toAuthority()로 GrantedAuthority 문자열을 제공 역할
 * @since 2025-08-13
 * @author jeongdayeon
 */
public enum Role {
    USER, ADMIN;

    public String toAuthority() {
        return "ROLE_" + name();
    }
}