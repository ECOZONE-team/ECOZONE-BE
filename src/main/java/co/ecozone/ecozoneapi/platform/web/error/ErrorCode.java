package co.ecozone.ecozoneapi.platform.web.error;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 표준 에러 코드 열거형
 *
 * - 각 항목은 (code, HttpStatus, defaultMessage, messageKey)을 보유
 * - 로그/대시보드/문서/관측에서 동일 식별자(code) 사용을 보장
 * - 도메인/보안/플랫폼 공통 에러를 한 곳에서 관리
 *
 * @author jeongdayeon
 * @since   2025-09-08
 */

@AllArgsConstructor
public enum ErrorCode {
    /**
     * 공통/플랫폼
     */
    BAD_REQUEST("COMMON-BAD-REQUEST", HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.", "error.common.badRequest"),
    VALIDATION_FAILED("COMMON-VALIDATION-FAILED", HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다.", "error.common.validation"),
    NOT_FOUND("COMMON-NOT-FOUND", HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다.", "error.common.notFound"),
    METHOD_NOT_ALLOWED("COMMON-METHOD-NOT-ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다.", "error.common.methodNotAllowed"),
    UNSUPPORTED_MEDIA("COMMON-UNSUPPORTED-MEDIA", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type 입니다.", "error.common.unsupportedMedia"),
    INTERNAL_ERROR("COMMON-INTERNAL", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.", "error.common.internal"),

    CONFLICT("COMMON-CONFLICT", HttpStatus.CONFLICT, "요청이 현재 리소스 상태와 충돌합니다.", "error.common.conflict"),
    ALREADY_EXISTS("COMMON-ALREADY-EXISTS", HttpStatus.CONFLICT, "이미 존재합니다.", "error.common.alreadyExists"),
    RESOURCE_LOCKED("COMMON-LOCKED", HttpStatus.LOCKED, "리소스가 잠겨 있습니다.", "error.common.locked"),
    TOO_MANY_REQUESTS("COMMON-TOO-MANY-REQUESTS", HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다.", "error.common.tooManyRequests"),
    SERVICE_UNAVAILABLE("COMMON-SERVICE-UNAVAILABLE", HttpStatus.SERVICE_UNAVAILABLE, "일시적으로 이용할 수 없습니다.", "error.common.serviceUnavailable"),

    /**
     * 보안/Auth
     */
    AUTH_REQUIRED("AUTH-REQUIRED", HttpStatus.UNAUTHORIZED, "인증이 필요합니다.", "error.auth.required"),
    AUTH_BAD_CREDENTIALS("AUTH-BAD-CREDENTIALS", HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.", "error.auth.badCredentials"),
    AUTH_BAD_TOKEN("AUTH-BAD-TOKEN", HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.", "error.auth.badToken"),
    AUTH_TOKEN_EXPIRED("AUTH-TOKEN-EXPIRED", HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다.", "error.auth.expired"),
    AUTH_ACCESS_DENIED("AUTH-ACCESS-DENIED", HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", "error.auth.accessDenied"),

    /**
     * Inquiry
     */
    INQUIRY_NOT_FOUND("INQUIRY-NOT-FOUND", HttpStatus.NOT_FOUND, "문의가 존재하지 않습니다.", "error.inquiry.notFound"),

    /**
     * Payment
     */
    PAYMENT_CONFIRM_FAILED("PAYMENT-CONFIRM-FAILED", HttpStatus.CONFLICT, "결제 승인에 실패했습니다.", "error.payment.confirmFailed"),
    PAYMENT_SESSION_NOT_FOUND("PAYMENT-SESSION-NOT-FOUND", HttpStatus.GONE, "결제 시간이 만료되었습니다.", "error.payment.sessionNotFound"),
    ;
    public final String code;

    public final HttpStatus status;
    // fallback 메시지
    public final String defaultMessage;
    // i18n key
    public final String messageKey;

}
