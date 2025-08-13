package co.ecozone.ecozoneapi.auth.domain.model;

/**
 * 도메인 사용자 식별자 값 객체
 * 생성 시 양수 여부를 검증
 * 불변(immutable) 속성
 * @since 2025-08-13
 * @author jeongdayeon
 */
public record UserId(Long value) {
    public UserId {
        if (value == null || value <= 0) throw new IllegalArgumentException("Invalid user id");
    }
}
