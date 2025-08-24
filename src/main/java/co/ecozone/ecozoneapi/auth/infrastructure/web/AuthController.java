package co.ecozone.ecozoneapi.auth.infrastructure.web;

import co.ecozone.ecozoneapi.auth.application.command.LogoutCommand;
import co.ecozone.ecozoneapi.auth.application.dto.Tokens;
import co.ecozone.ecozoneapi.auth.application.service.AuthService;
import co.ecozone.ecozoneapi.auth.infrastructure.web.mapper.AuthApiMapper;
import co.ecozone.ecozoneapi.auth.infrastructure.web.request.LoginRequest;
import co.ecozone.ecozoneapi.auth.infrastructure.web.request.RefreshRequest;
import co.ecozone.ecozoneapi.auth.infrastructure.web.request.SignUpRequest;
import co.ecozone.ecozoneapi.auth.infrastructure.web.response.TokenResponse;
import co.ecozone.ecozoneapi.auth.infrastructure.security.JwtPrincipal;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 REST 컨트롤러(웹 어댑터).
 * - 요청 DTO → 애플리케이션 커맨드 변환
 * - 서비스 결과 → 응답 DTO 매핑
 * @since 2025-08-23
 * @author jeongdayeon
 */

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthApiMapper mapper;

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@RequestBody @Valid SignUpRequest req) {
        authService.signUp(mapper.toCommand(req));
        return ResponseEntity.created(null).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest req) {
        Tokens tokens = authService.login(mapper.toCommand(req));
        return ResponseEntity.ok(mapper.toResponse(tokens));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Tokens> refresh(@RequestBody RefreshRequest req) {
        Tokens t = authService.refresh(req.refreshToken());
        return ResponseEntity.ok(t);
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal JwtPrincipal principal) {
        LogoutCommand cmd = new LogoutCommand(principal.userId(), principal.jti(), principal.expiresAt());
        authService.logout(cmd);
        return ResponseEntity.noContent().build();
    }

}
