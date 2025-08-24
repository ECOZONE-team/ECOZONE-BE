package co.ecozone.ecozoneapi.auth.infrastructure.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 API 입력 DTO
 * - 컨트롤러 바인딩 전용, 애플리케이션 커맨드로 변환되어 도메인에 전달
 * - 프레임워크 검증 어노테이션으로 1차 입력 검증 수행
 * @since 2025-08-23
 * @author jeongdayeon
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {}