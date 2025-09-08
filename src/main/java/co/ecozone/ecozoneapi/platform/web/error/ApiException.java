package co.ecozone.ecozoneapi.platform.web.error;

import lombok.Getter;

/**
 * 공통 예외 루트 타입 (도메인/애플리케이션 전용)
 * - ErrorCode를 포함하여 전역 핸들러(GlobalExceptionHandler)가 표준 ErrorResponse로 변환할 수 있게 함
 * - 메시지는 명시 문자열 또는 i18n 메시지 키(ErrorCode.messageKey)로 해석 가능
 * - 스택 트레이스 노출 등 보안 이슈를 중앙에서 통제하기 위한 어댑터 역할
 *
 * @author jeongdayeon
 * @since   2025-09-08
 */
@Getter
public class ApiException extends RuntimeException {
    private final ErrorCode code;
    private final Object[] args; // i18n 치환용

    public ApiException(ErrorCode code, String message, Object... args) {
        super(message);
        this.code = code;
        this.args = args;
    }

    public ApiException(ErrorCode code) {
        this(code, null);
    }
}