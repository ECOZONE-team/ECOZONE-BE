package co.ecozone.ecozoneapi.payment.application.port.in;

/**
 * 결제 취소 유스케이스 포트 (입력 포트)
 * - 애플리케이션 계층의 기능 계약을 정의
 * - 구현체는 서비스(PaymentService)에서 제공
 * @since 2025-09-16
 */
public interface CancelPaymentUseCase {
    Long cancel(Long paymentId, String reason);
}
