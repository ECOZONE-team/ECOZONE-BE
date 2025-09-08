package co.ecozone.ecozoneapi.platform.web.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

/**
 * 전역 예외 처리기 (ControllerAdvice)
 *
 * - ApiException 및 스프링 표준/검증/바인딩 예외를 표준 ErrorResponse로 일원화
 * - 로깅 정책: 4xx → warn(스택트레이스 최소), 5xx → error(스택 포함)
 * - MessageSource(i18n) 연동으로 다국어 메시지 지원
 * - SecurityEntryPoint/AccessDeniedHandler와 함께 JSON 응답 일관성 제공
 *
 * 주의:
 * - 민감정보(시크릿/토큰/PII)는 메시지나 로그에 포함하지 않음
 * - 상태코드/코드/메시지 표준은 ErrorCode로부터만 가져올 것
 *
 * @author jeongdayeon
 * @since   2025-09-08
 */
@Slf4j
@Order(0)
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageSource messageSource;
    private static final String HDR_REQ_ID = "X-Request-Id";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest req, Locale locale) {
        var code = ex.getCode();
        var msg  = resolveMessage(code, ex.getMessage(), ex.getArgs(), locale);
        logForStatus(code.status, ex);
        return build(code, msg, req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var violations = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toViolation).toList();
        log.warn("Validation failed: {} {}", req.getMethod(), req.getRequestURI());
        return build(ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.defaultMessage, req, violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        var violations = ex.getConstraintViolations().stream()
                .map(v -> new FieldViolation(v.getPropertyPath().toString(), v.getInvalidValue(), v.getMessage()))
                .toList();
        log.warn("Constraint violation: {} {}", req.getMethod(), req.getRequestURI());
        return build(ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.defaultMessage, req, violations);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest req) {
        log.warn("Bad request: {} {} - {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return build(ErrorCode.BAD_REQUEST, ErrorCode.BAD_REQUEST.defaultMessage, req, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return build(ErrorCode.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED.defaultMessage, req, List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedType(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return build(ErrorCode.UNSUPPORTED_MEDIA, ErrorCode.UNSUPPORTED_MEDIA.defaultMessage, req, List.of());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        var code = switch (ex.getStatusCode()) {
            case HttpStatusCode sc when sc.value() == 404 -> ErrorCode.NOT_FOUND;
            default -> ErrorCode.BAD_REQUEST;
        };
        logForStatus(HttpStatus.valueOf(ex.getStatusCode().value()), ex);
        return build(code, ex.getReason(), req, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandled(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error: {} {}", req.getMethod(), req.getRequestURI(), ex);
        return build(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.defaultMessage, req, List.of());
    }

    private FieldViolation toViolation(FieldError e) {
        return new FieldViolation(e.getField(), e.getRejectedValue(), e.getDefaultMessage());
    }

    private String resolveMessage(ErrorCode code, String explicit, Object[] args, Locale locale) {
        if (explicit != null && !explicit.isBlank()) return explicit;
        try {
            return messageSource.getMessage(code.messageKey, args, locale);
        } catch (Exception ignore) {
            return code.defaultMessage;
        }
    }

    private ResponseEntity<ErrorResponse> build(ErrorCode code, String msg, HttpServletRequest req, List<FieldViolation> errors) {
        var body = ErrorResponse.of(code, msg, req.getRequestURI(), req.getMethod(),
                req.getHeader(HDR_REQ_ID), errors);
        return ResponseEntity.status(code.status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    private void logForStatus(HttpStatus status, Exception ex) {
        if (status.is5xxServerError()) log.error("Server error", ex);
        else log.warn("Client error: {}", ex.getMessage());
    }

}
