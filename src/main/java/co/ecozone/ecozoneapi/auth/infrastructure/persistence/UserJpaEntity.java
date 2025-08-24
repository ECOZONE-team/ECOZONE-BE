package co.ecozone.ecozoneapi.auth.infrastructure.persistence;

import co.ecozone.ecozoneapi.auth.domain.model.security.Role;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;


/**
 * 사용자 테이블에 매핑되는 JPA 엔티티
 * - 스키마 제약(고유/널/길이)과 컬럼 정의를 명확히 기술
 * - 영속 모델이며, 도메인 애그리거트(User)와는 별도 모델
 * @since 2025-08-23
 * @author jeongdayeon
 */
@Getter
@Entity
@Table(name = "users", indexes = {
        @Index(name = "uk_users_email", columnList = "email", unique = true)
})
public class UserJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=190)
    private String email;

    @Column(nullable=false, length=100)
    private String name;

    @Column(name="password_hash", nullable=false, length=100)
    private String passwordHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="user_roles", joinColumns=@JoinColumn(name="user_id"))
    @Column(name="role", nullable=false, length=50)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = EnumSet.of(Role.USER);

    @Column(nullable=false)
    private boolean enabled = true;

    @Column(nullable=false)
    private Instant createdAt = Instant.now();
    protected UserJpaEntity() {}

    public static UserJpaEntity of(String email, String name, String passwordHash, Set<Role> roles) {
        UserJpaEntity e = new UserJpaEntity();
        e.email = email;
        e.name = name;
        e.passwordHash = passwordHash;
        e.roles = roles;
        return e;
    }
}
