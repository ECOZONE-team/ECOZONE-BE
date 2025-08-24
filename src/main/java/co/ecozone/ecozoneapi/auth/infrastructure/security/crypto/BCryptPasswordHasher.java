package co.ecozone.ecozoneapi.auth.infrastructure.security.crypto;

import co.ecozone.ecozoneapi.auth.domain.port.out.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * PasswordHasher 포트의 BCrypt 구현
 * - 평문 비밀번호 해시/검증 제공
 * - 보안 파라미터(스트렝스)는 프로퍼티로 외부화 권장
 * @since 2025-08-23
 * @author jeongdayeon
 */
@Component
public class BCryptPasswordHasher implements PasswordHasher {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    @Override public boolean matches(String raw, String hashed) {
        return encoder.matches(raw, hashed);
    }
}