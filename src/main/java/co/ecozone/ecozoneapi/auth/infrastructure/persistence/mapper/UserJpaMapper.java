package co.ecozone.ecozoneapi.auth.infrastructure.persistence.mapper;


import co.ecozone.ecozoneapi.auth.domain.model.User;
import co.ecozone.ecozoneapi.auth.domain.model.UserId;
import co.ecozone.ecozoneapi.auth.infrastructure.persistence.UserJpaEntity;
import co.ecozone.ecozoneapi.common.config.MapStructCentralConfig;
import org.mapstruct.*;

/**
 * 영속 어댑터 전용 매퍼
 * - JPA 엔티티(UserJpaEntity) ↔ 도메인(User) 상호 변환
 * - 지연 로딩/널 처리 등 인프라 세부는 여기서 캡슐화
 * @since 2025-08-23
 * @author jeongdayeon
 */
@Mapper(config = MapStructCentralConfig.class)
public interface UserJpaMapper {

    /** MapStruct가 User 인스턴스를 만들 때 호출할 팩토리 */
    @ObjectFactory
    default User createUser(UserJpaEntity e) {
        if (e == null) return null;
        return User.rehydrate(
                new UserId(e.getId()),
                e.getEmail(),
                e.getName(),
                e.getPasswordHash(),
                e.getRoles(),
                e.isEnabled(),
                e.getCreatedAt()
        );
    }

    /** 역방향: 엔티티 생성도 팩토리로 제공 */
    @ObjectFactory
    default UserJpaEntity createEntity(User u) {
        if (u == null) return null;
        return UserJpaEntity.of(
                u.getEmail(),
                u.getName(),
                u.getPasswordHash(),
                u.getRoles()
        );
    }

    @BeanMapping(ignoreByDefault = true)
    User toDomain(UserJpaEntity e);

    @BeanMapping(ignoreByDefault = true)
    UserJpaEntity toEntity(User u);

    @Named("toUserId")
    default UserId toUserId(Long id) { return (id == null) ? null : new UserId(id); }
}
