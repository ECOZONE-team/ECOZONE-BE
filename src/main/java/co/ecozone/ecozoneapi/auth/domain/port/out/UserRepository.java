package co.ecozone.ecozoneapi.auth.domain.port.out;


import co.ecozone.ecozoneapi.auth.domain.model.User;

import java.util.Optional;

/**
 * 사용자 애그리거트 저장소 Port
 * - 이메일 중복 체크/조회/저장 등의 기본 계약을 정의
 * - 구현은 인프라 계층(JPA 등)에서 제공
 * @since 2025-08-20
 * @author jeongdayeon
 */
public interface UserRepository {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    User save(User user);
}