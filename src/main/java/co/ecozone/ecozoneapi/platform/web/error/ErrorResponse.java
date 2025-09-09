package co.ecozone.ecozoneapi.platform.web.error;


import java.time.Instant;
import java.util.List;

/**
 * 표준 에러 응답 스키마 (클라이언트 반환용)
 *
 * - 필드: timestamp, status, error, code, message, path, method, requestId, errors
 * - 내부 표준 포맷(RFC7807 대체)으로 관측/문서화/클라이언트 처리 단순화
 * - 불변 record로 정의하여 스레드-세이프
 *
 * @author jeongdayeon
 * @since   2025-09-08
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        String method,
        String requestId,
        List<FieldViolation> errors
) {
    public static ErrorResponse of(
            ErrorCode code, String message, String path, String method, String requestId, List<FieldViolation> errors) {
        return new ErrorResponse(
                Instant.now(),
                code.status.value(),
                code.status.getReasonPhrase(),
                code.code,
                message != null ? message : code.defaultMessage,
                path, method, requestId,
                errors == null ? List.of() : errors
        );
    }
}
