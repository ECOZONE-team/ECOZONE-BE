package co.ecozone.ecozoneapi.auth.domain.model.token;

/**
 * 토큰 타입 도메인 열거형.
 * - ACCESS: 인증에 사용되는 단기 토큰
 * - REFRESH: 재발급 용도의 장기 토큰
 * @since 2025-08-20
 * @author jeongdayeon
 */
public enum TokenType {
    ACCESS, REFRESH;

    public static TokenType from(String v) {
        if (v == null) return null;
        return switch (v.toLowerCase()) {
            case "access" -> ACCESS;
            case "refresh" -> REFRESH;
            default -> null;
        };
    }
}