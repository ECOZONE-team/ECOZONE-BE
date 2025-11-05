package co.ecozone.ecozoneapi.payment.application.port.in;

import co.ecozone.ecozoneapi.auth.domain.model.UserId;

/**
 * 결제 승인 유스케이스 포트 (입력 포트)
 * - Idempotency-Key를 통한 동일 요청 재진입 제어 전제
 * - 구현체는 서비스(PaymentService)에서 제공
 * @since 2025-09-16
 */
public interface ConfirmPaymentUseCase {
    Long confirm(String idemKey, UserId userId, String paymentKey, String orderId, long amount);
}
