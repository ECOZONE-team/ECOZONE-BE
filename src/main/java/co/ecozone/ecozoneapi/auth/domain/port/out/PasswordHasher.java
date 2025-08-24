package co.ecozone.ecozoneapi.auth.domain.port.out;

/**
 * 비밀번호 해시/검증 Port
 * 구체 구현은 인프라에서 - 도메인/유스케이스는 구현체(BCrypt/Argon2 등)에 독립
 * @since 2025-08-20
 **/
public interface PasswordHasher {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String hashed);
}