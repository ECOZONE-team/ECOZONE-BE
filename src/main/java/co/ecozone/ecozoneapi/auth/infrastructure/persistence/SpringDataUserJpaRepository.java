package co.ecozone.ecozoneapi.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA 리포지토리
 * - 영속 계층의 쿼리 메서드 정의(이메일 조회 등)
 * - 도메인 포트(UserRepository) 구현 어댑터와 조합되어 사용
 * @since 2025-08-23
 * @author jeongdayeon
 */
public interface SpringDataUserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    boolean existsByEmail(String email);
    Optional<UserJpaEntity> findByEmail(String email);
}