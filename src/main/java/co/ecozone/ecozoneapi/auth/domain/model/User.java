package co.ecozone.ecozoneapi.auth.domain.model;

import co.ecozone.ecozoneapi.auth.domain.model.security.Role;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * 사용자 Aggregate Root
 * - 비즈니스 관점의 사용자 모델 (영속 모델과 분리)
 * - 불변 컬렉션, 생성 시 유효성 검증
 * - 이메일/표시이름/비밀번호해시/역할/활성 여부/생성시각
 * @since 2025-08-20
 */
@Getter
public class User {
    private final UserId id;

    // 이메일(고유)
    private final String email;

    // 표시 이름
    private final String name;

    // 해시(평문 금지)
    private final String passwordHash;

    // 최소 USER
    private final Set<Role> roles;
    private final boolean enabled;
    private final Instant createdAt;

    private User(UserId id, String email, String name, String passwordHash,
                 Set<Role> roles, boolean enabled, Instant createdAt) {
        if (email == null || !email.contains("@")) throw new IllegalArgumentException("invalid email");
        if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("passwordHash required");
        this.id = id;
        this.email = email;
        this.name = name != null ? name : "";
        this.passwordHash = passwordHash;
        this.roles = Collections.unmodifiableSet(roles != null && !roles.isEmpty() ? EnumSet.copyOf(roles) : EnumSet.of(Role.USER));
        this.enabled = enabled;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static User createNew(String email, String name, String passwordHash) {
        return new User(null, email, name, passwordHash, EnumSet.of(Role.USER), true, Instant.now());
    }
    public static User rehydrate(UserId id, String email, String name, String passwordHash,
                                 Set<Role> roles, boolean enabled, Instant createdAt) {
        return new User(id, email, name, passwordHash, roles, enabled, createdAt);
    }

    public User withId(UserId id) {
        return new User(id, email, name, passwordHash, roles, enabled, createdAt);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return Objects.equals(id, other.id) && email.equals(other.email);
    }
    @Override public int hashCode() { return Objects.hash(id, email); }

}
