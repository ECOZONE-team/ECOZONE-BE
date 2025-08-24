package co.ecozone.ecozoneapi.auth.infrastructure.web.request;

/**
 * 리프레시 토큰으로 재발급 호출 시 사용하는 API 입력 DTO.
 * - 토큰은 일반적으로 HttpOnly 쿠키를 권장하나, 초기 단계에서는 바디로 수신
 * @since 2025-08-23
 * @author jeongdayeon
 */
public record RefreshRequest(String refreshToken) {}
