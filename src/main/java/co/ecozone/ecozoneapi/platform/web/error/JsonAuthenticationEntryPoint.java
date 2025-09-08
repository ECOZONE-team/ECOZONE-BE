package co.ecozone.ecozoneapi.platform.web.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Spring Security AuthenticationEntryPoint (401) JSON 응답 구현
 *
 * - 인증이 없는 요청에 대해 ErrorCode.AUTH_REQUIRED 표준 ErrorResponse 반환
 * - 브라우저 기본 인증 팝업/리다이렉트 대신 JSON 바디 응답 고정
 *
 * 통합 위치:
 * - SecurityConfig.exceptionHandling().authenticationEntryPoint(new JsonAuthenticationEntryPoint(objectMapper))
 *
 * @author jeongdayeon
 * @since   2025-09-08
 */

@Slf4j
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper om;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException e) {
        try {
            var body = ErrorResponse.of(ErrorCode.AUTH_REQUIRED, ErrorCode.AUTH_REQUIRED.defaultMessage,
                    req.getRequestURI(), req.getMethod(), req.getHeader("X-Request-Id"), null);
            res.setStatus(ErrorCode.AUTH_REQUIRED.status.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            om.writeValue(res.getOutputStream(), body);
        } catch (Exception ex) {
            log.error("AuthEntryPoint write failed", ex);
        }
    }
}
