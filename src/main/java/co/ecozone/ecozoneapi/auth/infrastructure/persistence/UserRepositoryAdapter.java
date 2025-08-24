package co.ecozone.ecozoneapi.auth.infrastructure.persistence;

import co.ecozone.ecozoneapi.auth.domain.model.User;
import co.ecozone.ecozoneapi.auth.domain.port.out.UserRepository;
import co.ecozone.ecozoneapi.auth.infrastructure.persistence.mapper.UserJpaMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 도메인 포트(UserRepository)의 JPA 구현 어댑터
 * - 엔티티 ↔ 도메인 변환과 영속 예외를 도메인 친화적으로 변환
 * - 트랜잭션 경계는 서비스/유스케이스 계층에서 관리
 * @since 2025-08-23
 * @author jeongdayeon
 */
@Repository
@AllArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserJpaRepository jpa;
    private final UserJpaMapper mapper;


    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = mapper.toEntity(user);
        UserJpaEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }
}
