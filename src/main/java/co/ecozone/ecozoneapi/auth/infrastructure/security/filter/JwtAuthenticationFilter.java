package co.ecozone.ecozoneapi.auth.infrastructure.security.filter;

import co.ecozone.ecozoneapi.auth.application.port.TokenRevocationStore;
import co.ecozone.ecozoneapi.auth.domain.model.security.Role;
import co.ecozone.ecozoneapi.auth.domain.model.token.TokenFailure;
import co.ecozone.ecozoneapi.auth.domain.model.token.TokenSuccess;
import co.ecozone.ecozoneapi.auth.domain.model.token.TokenType;
import co.ecozone.ecozoneapi.auth.domain.model.token.TokenVerification;
import co.ecozone.ecozoneapi.auth.domain.port.out.TokenProvider;
import co.ecozone.ecozoneapi.auth.infrastructure.security.JwtPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final TokenRevocationStore revocationStore;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String raw = header.substring(7);

            try {
                TokenVerification v = tokenProvider.verify(raw);

                if (v instanceof TokenSuccess s) {
                    if (s.tokenType() != TokenType.ACCESS) {
                        chain.doFilter(request, response);
                        return;
                    }
                    if (!revocationStore.isRevoked(s.tokenId())) {
                        Authentication auth = toAuthentication(s);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        SecurityContextHolder.clearContext();
                        request.setAttribute("auth.error", "revoked");
                        log.debug("JWT revoked: jti={}", s.tokenId());
                    }

                } else if (v instanceof TokenFailure f) {
                    SecurityContextHolder.clearContext();
                    request.setAttribute("auth.error", f.reason().name());
                    log.debug("JWT invalid: reason={}, msg={}", f.reason(), f.message());
                }

            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                request.setAttribute("auth.error", "exception");
                log.warn("JWT filter exception", e);
            }
        }

        chain.doFilter(request, response);
    }

    private Authentication toAuthentication(TokenSuccess s) {
        var authorities = s.roles().stream()
                .map(Role::toAuthority)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        JwtPrincipal principal = new JwtPrincipal(
                s.userId(), s.roles(), s.issuedAt(), s.expiresAt(), s.tokenId(), s.issuer()
        );

        return new UsernamePasswordAuthenticationToken(principal, "N/A", authorities);
    }
}