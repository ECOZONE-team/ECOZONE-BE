package co.ecozone.ecozoneapi.payment.application.port.out;

import co.ecozone.ecozoneapi.payment.domain.model.PaymentEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 결제 이벤트 저장소 포트 (Webhook용)
 * - PG로부터 수신한 이벤트 추적
 * @since 2025-01-15
 */
public interface PaymentEventRepository {

    /**
     * 이벤트 저장
     */
    PaymentEvent save(PaymentEvent event);

    /**
     * 이벤트 ID로 조회 (멱등성 체크용)
     */
    Optional<PaymentEvent> findByEventId(String eventId);

    /**
     * orderId로 조회
     */
    List<PaymentEvent> findByOrderId(String orderId);

    /**
     * 미처리 이벤트 조회
     */
    List<PaymentEvent> findPendingEvents();
}
