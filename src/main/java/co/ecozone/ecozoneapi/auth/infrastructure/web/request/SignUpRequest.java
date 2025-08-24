package co.ecozone.ecozoneapi.auth.infrastructure.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 회원가입 API 입력 DTO.
 * - 이메일/표시이름/평문 비밀번호를 수신하여 커맨드로 변환
 * @since 2025-08-23
 * @author jeongdayeon
 */
public record SignUpRequest(
        @NotBlank @Email String email,
        String name,                 // 선택 입력
        @NotBlank String password
) {}