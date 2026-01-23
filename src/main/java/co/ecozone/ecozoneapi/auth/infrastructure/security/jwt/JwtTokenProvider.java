package co.ecozone.ecozoneapi.auth.infrastructure.security.jwt;

import co.ecozone.ecozoneapi.auth.domain.model.*;
import co.ecozone.ecozoneapi.auth.domain.model.security.Role;
import co.ecozone.ecozoneapi.auth.domain.model.token.*;
import co.ecozone.ecozoneapi.auth.domain.port.out.TokenProvider;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Nimbus JOSE/JWT 기반의 TokenProvider 구현체(HS256)
 * 액세스/리프레시 토큰 서명과 exp/nbf/iat 검증을 수행
 * roles/sub/jti/issuer 클레임을 처리
 * 계층: 인프라(보안 어댑터)
 * @since 2025-08-13
 * @author jeongdayeon
 */
@Slf4j
@Component
@RequiredArgsConstructor
class JwtTokenProvider implements TokenProvider {

    private final JwtProperties props;
    private final Clock clock;

    private byte[] key() {
        // base64 디코드 우선, 실패하면 평문 bytes
        try { return Base64.getDecoder().decode(props.getBase64Secret()); }
        catch (IllegalArgumentException e) { return props.getBase64Secret().getBytes(StandardCharsets.UTF_8); }
    }

    @Override
    public String generateAccessToken(UserId userId, Set<Role> roles) {
        return sign(userId, roles, props.getAccessTtlSeconds(), "access");
    }

    @Override
    public String generateRefreshToken(UserId userId) {
        return sign(userId, Set.of(Role.USER), props.getRefreshTtlSeconds(), "refresh");
    }

    private String sign(UserId userId, Set<Role> roles, long ttlSeconds, String type) {
        Instant now = Instant.now(clock);
        Instant exp = now.plusSeconds(ttlSeconds);

        List<String> roleNames = roles.stream().map(Enum::name).toList();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(props.getIssuer())
                .subject(String.valueOf(userId.value()))
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .jwtID(UUID.randomUUID().toString())
                .claim("typ", type)
                .claim("roles", roleNames)
                .build();

        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build();
            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(new MACSigner(key()));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("JWT signing failed", e);
        }
    }

    @Override
    public TokenVerification verify(String jwtString) {
        try {
            SignedJWT jwt = SignedJWT.parse(jwtString);
            if (!jwt.verify(new MACVerifier(key()))) {
                return new TokenFailure(jwtString, VerificationError.BAD_SIGNATURE, "invalid signature");
            }

            JWTClaimsSet c = jwt.getJWTClaimsSet();
            Instant now = Instant.now(clock);
            long skew = props.getClockSkewSeconds();

            Instant exp = Optional.ofNullable(c.getExpirationTime()).map(Date::toInstant).orElse(null);
            if (exp != null && now.isAfter(exp.plusSeconds(skew))) {
                return new TokenFailure(jwtString, VerificationError.EXPIRED, "expired at " + exp);
            }

            // nbf(not before) 우선 검사
            Instant nbf = Optional.ofNullable(c.getNotBeforeTime()).map(Date::toInstant).orElse(null);
            if (nbf != null && now.isBefore(nbf.minusSeconds(skew))) {
                return new TokenFailure(jwtString, VerificationError.NOT_BEFORE, "not before " + nbf);
            }

            // iat(issued at)가 미래인 경우도 거부
            Instant iat = Optional.ofNullable(c.getIssueTime()).map(Date::toInstant).orElse(null);
            if (iat != null && now.isBefore(iat.minusSeconds(skew))) {
                return new TokenFailure(jwtString, VerificationError.NOT_BEFORE, "issued at " + iat);
            }

            String typClaim = c.getStringClaim("typ");
            TokenType type = TokenType.from(typClaim);
            if (type == null) {
                return new TokenFailure(jwtString, VerificationError.MALFORMED, "unknown typ: " + typClaim);
            }

            String sub = c.getSubject();
            if (sub == null) {
                return new TokenFailure(jwtString, VerificationError.MALFORMED, "missing subject");
            }

            @SuppressWarnings("unchecked")
            var roleNames = Optional.ofNullable((List<String>) c.getClaim("roles")).orElse(List.of());
            Set<Role> roles = roleNames.stream()
                    .map(r -> {
                        try {
                            return Role.valueOf(r);
                        } catch (IllegalArgumentException e) {
                            // 🔒 알 수 없는 role 경고 로그
                            log.warn("Unknown role in JWT: {} - defaulting to USER", r);
                            return Role.USER;  // 기본값으로 설정
                        }
                    })
                    .collect(Collectors.toSet());

// 🔒 roles가 비어있으면 최소한 USER 역할 부여
            if (roles.isEmpty()) {
                roles = Set.of(Role.USER);
            }


            return new TokenSuccess(
                    type,
                    new UserId(Long.parseLong(sub)),
                    roles,
                    iat, exp,
                    c.getIssuer(), c.getJWTID(),
                    jwtString
            );

        } catch (ParseException e) {
            return new TokenFailure(jwtString, VerificationError.MALFORMED, e.getMessage());
        } catch (JOSEException e) {
            return new TokenFailure(jwtString, VerificationError.BAD_SIGNATURE, e.getMessage());
        } catch (Exception e) {
            return new TokenFailure(jwtString, VerificationError.UNKNOWN, e.getMessage());
        }
    }

}