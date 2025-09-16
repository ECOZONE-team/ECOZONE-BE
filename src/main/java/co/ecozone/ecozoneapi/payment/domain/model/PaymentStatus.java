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
    INITIATED,
    /*
    ** 인증/가승인 완료(카드사 등)
     */
    AUTHORIZED,
    /*
    ** 상점 승인(서버 confirm 완료)
     */
    CONFIRMED,
    /*
    ** 상점 취소
     */
    CANCELED,
    /*
    ** 실패(승인/취소 등)
     */
    FAILED
}