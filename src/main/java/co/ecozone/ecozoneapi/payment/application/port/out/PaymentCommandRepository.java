package co.ecozone.ecozoneapi.payment.application.port.out;

import co.ecozone.ecozoneapi.payment.domain.model.PaymentCommand;

import java.util.Optional;

/**
 * 결제 명령 저장소 포트
 * - 멱등성 보장을 위한 명령 추적
 * @since 2025-01-15
 */
public interface PaymentCommandRepository {

    /**
     * 명령 저장
     */
    PaymentCommand save(PaymentCommand command);

    /**
     * 멱등키로 조회
     */
    Optional<PaymentCommand> findByIdempotencyKey(String idempotencyKey);

    /**
     * ID로 조회
     */
    Optional<PaymentCommand> findById(Long id);

    /**
     * orderId로 조회
     */
    Optional<PaymentCommand> findByOrderId(String orderId);
}
