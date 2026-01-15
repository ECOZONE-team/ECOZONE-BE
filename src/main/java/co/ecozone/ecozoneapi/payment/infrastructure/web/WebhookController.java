package co.ecozone.ecozoneapi.payment.infrastructure.web;

import co.ecozone.ecozoneapi.payment.application.service.WebhookService;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentEvent;
import co.ecozone.ecozoneapi.payment.infrastructure.web.request.TossWebhookRequest;
import co.ecozone.ecozoneapi.payment.infrastructure.web.response.WebhookResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * Webhook 수신 컨트롤러
 * - Toss Payments 웹훅 수신
 * - 서명 검증 (TODO: 구현 필요)
 * @since 2025-01-15
 */
@Slf4j
@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;

    /**
     * Toss Payments 웹훅 수신
     */
    @PostMapping
    public ResponseEntity<WebhookResponse> handleTossWebhook(
            @RequestHeader(value = "X-Toss-Signature", required = false) String signature,
            @RequestBody TossWebhookRequest request
    ) {
        log.info("Received webhook: eventId={}, eventType={}, orderId={}",
                request.eventId(), request.eventType(), request.orderId());

        // TODO: 서명 검증 (Toss에서 제공하는 signature 검증)
        // if (!verifySignature(signature, requestBody)) {
        //     return ResponseEntity.status(401).build();
        // }

        try {
            // 페이로드를 JSON 문자열로 변환
            String payload = objectMapper.writeValueAsString(request);

            // 이벤트 타입 매핑
            PaymentEvent.EventType eventType = mapEventType(request.eventType());

            // Webhook 처리
            Long eventId = webhookService.handleWebhook(
                    request.eventId(),
                    eventType,
                    request.orderId(),
                    request.paymentKey(),
                    payload,
                    Instant.parse(request.occurredAt())
            );

            return ResponseEntity.ok(new WebhookResponse("success", eventId));

        } catch (Exception e) {
            log.error("Failed to process webhook: eventId={}, error={}",
                    request.eventId(), e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new WebhookResponse("error", null));
        }
    }

    /**
     * Toss 이벤트 타입을 도메인 이벤트 타입으로 매핑
     */
    private PaymentEvent.EventType mapEventType(String tossEventType) {
        return switch (tossEventType) {
            case "PAYMENT_CONFIRMED", "payment.confirmed" -> PaymentEvent.EventType.PAYMENT_CONFIRMED;
            case "PAYMENT_CANCELED", "payment.canceled" -> PaymentEvent.EventType.PAYMENT_CANCELED;
            case "PAYMENT_FAILED", "payment.failed" -> PaymentEvent.EventType.PAYMENT_FAILED;
            case "VIRTUAL_ACCOUNT_ISSUED" -> PaymentEvent.EventType.VIRTUAL_ACCOUNT_ISSUED;
            case "VIRTUAL_ACCOUNT_DEPOSITED" -> PaymentEvent.EventType.VIRTUAL_ACCOUNT_DEPOSITED;
            default -> throw new IllegalArgumentException("Unknown event type: " + tossEventType);
        };
    }
}
