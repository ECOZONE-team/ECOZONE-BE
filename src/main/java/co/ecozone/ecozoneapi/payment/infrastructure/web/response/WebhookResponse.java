package co.ecozone.ecozoneapi.payment.infrastructure.web.response;

/**
 * Webhook 응답 DTO
 * @since 2025-01-15
 */
public record WebhookResponse(
        String status,    // success or error
        Long eventId      // 처리된 이벤트 ID
) {}
