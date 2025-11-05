package co.ecozone.ecozoneapi.payment.domain.model;

/**
 * 결제 상태 Enum
 * - INITIATED/AUTHORIZED/CONFIRMED/CANCELED/FAILED
 * - 상태 전이 규칙은 도메인(Payment)에서만 관리
 * @since 2025-09-16
 */
public enum PaymentStatus {
    /*
    ** 결제 시작(클라이언트 결제 시도 직전)
     */
    INITIATED(false),
    /*
    ** 인증/가승인 완료(카드사 등)
     */
    AUTHORIZED(false),
    /*
    ** PSP에서 승인 완료(서버 confirm 완료)
     */
    CONFIRMED(true),
    /*
    ** 상점/사용자 취소
     */
    CANCELED(true),
    /*
    ** 실패(승인/취소 등)
     */
    FAILED(true);

    private final boolean terminal;

    PaymentStatus(boolean terminal) {
        this.terminal = terminal;
    }

    /** 더 이상 상태 전이가 일어나지 않는 종결 상태인지 */
    public boolean isTerminal() {
        return terminal;
    }

    /** 유효한 전이만 허용 (옵션: 도메인 메서드에서 사용) */
    public boolean canTransitionTo(PaymentStatus next) {
        return switch (this) {
            case INITIATED   -> next == AUTHORIZED || next == FAILED || next == CANCELED;
            case AUTHORIZED  -> next == CONFIRMED || next == FAILED || next == CANCELED;
            case CONFIRMED, FAILED, CANCELED -> false; // 종결 이후 전이 불가
        };
    }
}