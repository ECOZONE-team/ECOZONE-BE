package co.ecozone.ecozoneapi.auth.infrastructure.security.filter;

import co.ecozone.ecozoneapi.auth.domain.model.TokenFailure;
import co.ecozone.ecozoneapi.auth.domain.model.Role;
import co.ecozone.ecozoneapi.auth.domain.model.TokenSuccess;
import co.ecozone.ecozoneapi.auth.domain.model.TokenVerification;
import co.ecozone.ecozoneapi.auth.domain.port.out.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * 요청의 인증 헤더를 읽어 TokenProvider로 검증하고
 * 성공 시 SecurityContext를 구성한다. 실패 시 요청 속성에 사유/메시지를 기록하여
 * AuthenticationEntryPoint가 일관된 401 JSON을 반환
 * 하드코딩된 헤더/스킴은 HeaderProperties를 통해 외부화
 * @since 2025-08-13
 * @author jeongdayeon
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            TokenVerification v = tokenProvider.verify(token);
            if (v instanceof TokenSuccess s) {
                Authentication auth = toAuthentication(s);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else if (v instanceof TokenFailure f) {
                SecurityContextHolder.clearContext();

            }
        }
        chain.doFilter(request, response);
    }

    private Authentication toAuthentication(TokenSuccess s) {
        var authorities = s.roles().stream()
                .map(Role::toAuthority)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        AbstractAuthenticationToken auth =
                new PreAuthenticatedAuthenticationToken(s.userId().value(), s.raw(), authorities);
        auth.setAuthenticated(true);
        return auth;
    }
}