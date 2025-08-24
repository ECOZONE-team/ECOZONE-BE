package co.ecozone.ecozoneapi.auth.infrastructure.web.response;

/**
 * 인증 응답 DTO.
 * - Bearer 타입, Access/Refresh 토큰, UX용 만료 초(expiresIn) 포함
 * - 외부 계약(API 스키마) 전용 타입
 * @since 2025-08-23
 * @author jeongdayeon
 */
public record TokenResponse(String tokenType, String accessToken, String refreshToken, long expiresInSeconds) {}