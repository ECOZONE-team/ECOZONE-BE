package co.ecozone.ecozoneapi.auth.domain.model;

/**
 * 엔드포인트 접근 수준을 나타내는 열거형
 * 현재는 PERMIT_ALL, AUTHENTICATED, ROLE_BASED 세 단계를 지원
 * @since 2025-08-13
 * @author jeongdayeon
 */
public enum AccessLevel {
    /**
     * 전체 허용
     */
    PERMIT_ALL,
    /**
     * 인증된 사용자
     */
    AUTHENTICATED,
    /**
     * 권한 기반
     */
    ROLE_BASED
}