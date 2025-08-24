package co.ecozone.ecozoneapi.auth.application.command;

/**
 * 로그인 유스케이스 입력 모델
 * - 컨트롤러(웹 어댑터) → 애플리케이션 서비스로 전달되는 순수 커맨드
 * @since 2025-08-20
 */
public record LoginCommand(String email, String password) {
    public LoginCommand {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email required");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("password required");
    }
}
