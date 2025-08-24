package co.ecozone.ecozoneapi.auth.application.command;

/**
 * 회원가입 유스케이스 입력 모델
 * - 이메일/이름/평문 비밀번호를 애플리케이션 서비스로 전달
 * - 컨트롤러에서 1차 바인딩/검증 후 불변 객체로 전달
 * @since 2025-08-20
 */
public record SignUpCommand(String email, String name, String password) {
    public SignUpCommand {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email required");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("password required");
        // name은 선택 입력 → null이면 빈 문자열로 정규화
        name = (name == null) ? "" : name;
    }
}