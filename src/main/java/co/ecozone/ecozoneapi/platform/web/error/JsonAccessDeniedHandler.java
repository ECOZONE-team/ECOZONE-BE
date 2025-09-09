package co.ecozone.ecozoneapi.platform.web.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Spring Security AccessDeniedHandler (403) JSON 응답 구현
 *
 * - 인증은 되었으나 권한이 부족한 경우(ErrorCode.AUTH_ACCESS_DENIED) 표준 ErrorResponse 반환
 * - HTML 리다이렉트 대신 JSON 바디로 고정하여 API 일관성 확보
 *
 * 통합 위치:
 * - SecurityConfig.exceptionHandling().accessDeniedHandler(new JsonAccessDeniedHandler(objectMapper))
 *
 * @author jeongdayeon
 * @since   2025-09-08
 */
@Slf4j
@RequiredArgsConstructor
public class JsonAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper om;

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException e) {
        try {
            var body = ErrorResponse.of(ErrorCode.AUTH_ACCESS_DENIED, ErrorCode.AUTH_ACCESS_DENIED.defaultMessage,
                    req.getRequestURI(), req.getMethod(), req.getHeader("X-Request-Id"), null);
            res.setStatus(ErrorCode.AUTH_ACCESS_DENIED.status.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            om.writeValue(res.getOutputStream(), body);
        } catch (Exception ex) {
            log.error("AccessDenied write failed", ex);
        }
    }
}