package co.ecozone.ecozoneapi.auth.domain.model;

/**
 * 토큰 검증의 결과를 나타내는 봉인(Sealed) 인터페이스
 * Success/Failure 두 구현만 허용하여 컴파일 타임에 분기 누락을 방지
 * @since 2025-08-13
 * @author jeongdayeon
 */
public sealed interface TokenVerification
        permits TokenFailure, TokenSuccess {
    boolean valid();
    String raw();
}