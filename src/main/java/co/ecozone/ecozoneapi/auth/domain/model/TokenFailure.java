package co.ecozone.ecozoneapi.auth.domain.model;

/**
 * 토큰 검증 실패를 표현하는 도메인 결과 타입
 * 실패 사유(VerificationError)와 메시지를 담고, API 401 응답 포맷에 활용
 * @since 2025-08-13
 * @author jeongdayeon
 */
public record TokenFailure(
        String raw, VerificationError reason, String message
) implements TokenVerification { @Override public boolean valid() { return false; } }

