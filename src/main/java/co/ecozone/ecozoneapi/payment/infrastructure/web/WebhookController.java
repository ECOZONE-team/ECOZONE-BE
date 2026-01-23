package co.ecozone.ecozoneapi.payment.infrastructure.web;

import co.ecozone.ecozoneapi.payment.application.service.WebhookService;
import co.ecozone.ecozoneapi.payment.domain.model.EventType;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentEvent;
import co.ecozone.ecozoneapi.payment.infrastructure.web.request.TossWebhookRequest;
import co.ecozone.ecozoneapi.payment.infrastructure.web.response.WebhookResponse;
import co.ecozone.ecozoneapi.payment.infrastructure.webhook.TossEventMapper;
import co.ecozone.ecozoneapi.payment.infrastructure.webhook.TossWebhookVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * Webhook 수신 컨트롤러
 * - HTTP 요청/응답 처리만 담당 (Thin Controller)
 * - 서명 검증, 타입 변환은 전문 컴포넌트에 위임
 * @since 2025-01-15
 */
@Slf4j
@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;
    private final TossWebhookVerifier webhookVerifier;
    private final TossEventMapper eventMapper;

    /**
     * Toss Payments 웹훅 수신
     */
    @PostMapping
    public ResponseEntity<WebhookResponse> handleTossWebhook(
            @RequestHeader(value = "X-Toss-Signature", required = true) String signature,
            @RequestBody String rawBody
    ) {
        try {
            // 1. 서명 검증 (위임)
            if (!webhookVerifier.verify(signature, rawBody)) {
                log.warn("Invalid webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new WebhookResponse("invalid_signature", null));
            }

            // 2. JSON 파싱
            TossWebhookRequest request = objectMapper.readValue(rawBody, TossWebhookRequest.class);

            log.info("Received webhook: eventId={}, eventType={}, orderId={}",
                    request.eventId(), request.eventType(), request.orderId());

            // 3. 타입 변환 (위임)
            EventType eventType = eventMapper.toDomainEventType(request.eventType());

            // 4. 비즈니스 로직 실행 (위임)
            Long eventId = webhookService.handleWebhook(
                    request.eventId(),
                    eventType,
                    request.orderId(),
                    request.paymentKey(),
                    rawBody,
                    Instant.parse(request.occurredAt())
            );

            return ResponseEntity.ok(new WebhookResponse("success", eventId));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid webhook data: error={}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new WebhookResponse("invalid_input", null));
        } catch (Exception e) {
            log.error("Failed to process webhook: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new WebhookResponse("processing_error", null));
        }
    }
}
