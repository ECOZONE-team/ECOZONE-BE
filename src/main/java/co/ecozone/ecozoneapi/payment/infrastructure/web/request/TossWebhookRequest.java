package co.ecozone.ecozoneapi.payment.infrastructure.web.request;

/**
 * Toss Payments Webhook 요청 DTO
 * @since 2025-01-15
 */
public record TossWebhookRequest(
        String eventId,       // 이벤트 ID (멱등키)
        String eventType,     // 이벤트 타입 (예: PAYMENT_CONFIRMED)
        String orderId,       // 주문번호
        String paymentKey,    // 결제키
        String occurredAt,    // 이벤트 발생 시각 (ISO-8601)
        Object data           // 추가 데이터
) {}
