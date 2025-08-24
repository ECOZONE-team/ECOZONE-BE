package co.ecozone.ecozoneapi.auth.application.dto;


/**
 * 인증 응답 DTO
 * - Bearer 타입, Access/Refresh 토큰, UX용 expiresIn(초) 포함
 * @since 2025-08-20
 */
public record Tokens(String tokenType, String accessToken, String refreshToken, long expiresInSeconds) {
    public static Tokens bearer(String access, String refresh, long expSec) {
        return new Tokens("Bearer", access, refresh, expSec);
    }
}