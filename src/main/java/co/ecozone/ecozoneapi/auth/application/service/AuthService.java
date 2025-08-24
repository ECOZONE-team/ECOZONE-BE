package co.ecozone.ecozoneapi.auth.application.service;

import co.ecozone.ecozoneapi.auth.application.command.LoginCommand;
import co.ecozone.ecozoneapi.auth.application.command.LogoutCommand;
import co.ecozone.ecozoneapi.auth.application.command.SignUpCommand;
import co.ecozone.ecozoneapi.auth.application.dto.Tokens;
import co.ecozone.ecozoneapi.auth.application.port.TokenPolicy;
import co.ecozone.ecozoneapi.auth.application.port.TokenRevocationStore;
import co.ecozone.ecozoneapi.auth.domain.model.*;
import co.ecozone.ecozoneapi.auth.domain.model.security.Role;
import co.ecozone.ecozoneapi.auth.domain.model.token.TokenSuccess;
import co.ecozone.ecozoneapi.auth.domain.model.token.TokenType;
import co.ecozone.ecozoneapi.auth.domain.port.out.PasswordHasher;
import co.ecozone.ecozoneapi.auth.domain.port.out.TokenProvider;
import co.ecozone.ecozoneapi.auth.domain.port.out.UserRepository;
import co.ecozone.ecozoneapi.auth.infrastructure.redis.RedisRefreshTokenRegistry;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * 인증 유스케이스 서비스
 * - 로그인/리프레시/로그아웃/회전 정책 수행 (도메인 규칙의 조합)
 * @since 2025-08-23
 */
@Service
@Transactional
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final TokenPolicy tokenPolicy;
    private final TokenRevocationStore revocationStore;
    private final RedisRefreshTokenRegistry refreshRegistry;
    private final Clock clock;

    public User signUp(SignUpCommand cmd) {
        Objects.requireNonNull(cmd);
        if (userRepository.existsByEmail(cmd.email())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        String hashed = passwordHasher.hash(cmd.password());
        User saved = userRepository.save(User.createNew(cmd.email(), cmd.name(), hashed));
        return saved;
    }

    public Tokens login(LoginCommand cmd) {
        User user = userRepository.findByEmail(cmd.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordHasher.matches(cmd.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        Set<Role> roles = EnumSet.copyOf(user.getRoles());
        String access = tokenProvider.generateAccessToken(new UserId(user.getId().value()), roles);
        String refresh = tokenProvider.generateRefreshToken(new UserId(user.getId().value()));

        var s = (TokenSuccess) tokenProvider.verify(refresh); // typ=refresh
        refreshRegistry.setCurrent(user.getId(), s.tokenId(), s.expiresAt());

        // expiresInSeconds는 Access 만료만 노출 (UX 기준)
        long expiresIn = Math.max(0, tokenPolicy.accessTtlSeconds() - tokenPolicy.clockSkewSeconds());
        return new Tokens("Bearer", access, refresh, expiresIn);
    }

    public Tokens refresh(String refreshToken) {
        var v = tokenProvider.verify(refreshToken);
        if (!(v instanceof TokenSuccess s) || s.tokenType() != TokenType.REFRESH) {
            throw new IllegalArgumentException("리프레시 토큰이 유효하지 않습니다.");
        }
        if (revocationStore.isRevoked(s.tokenId()) || !refreshRegistry.isCurrent(s.userId(), s.tokenId())) {
            throw new IllegalArgumentException("리프레시 토큰이 만료되었거나 이미 사용되었습니다.");
        }

        String newAccess = tokenProvider.generateAccessToken(s.userId(), s.roles());

        String newRefresh = tokenProvider.generateRefreshToken(s.userId());
        var s2 = (TokenSuccess) tokenProvider.verify(newRefresh);

        revocationStore.revoke(s.tokenId(), s.expiresAt());
        refreshRegistry.setCurrent(s.userId(), s2.tokenId(), s2.expiresAt());

        long expiresIn = Math.max(0, tokenPolicy.accessTtlSeconds() - tokenPolicy.clockSkewSeconds());
        return new Tokens("Bearer", newAccess, newRefresh, expiresIn);
    }



    public void logout(LogoutCommand cmd) {
        // TTL = max(1s, exp - now)
        Instant now = Instant.now(clock);
        Instant exp  = cmd.expiresAt() != null ? cmd.expiresAt() : now;
        long seconds = Math.max(1, Duration.between(now, exp).getSeconds());

        revocationStore.revoke(cmd.jti(), exp);
        refreshRegistry.invalidate(cmd.userId());
    }
}
