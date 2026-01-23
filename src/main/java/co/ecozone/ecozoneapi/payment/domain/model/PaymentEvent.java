package co.ecozone.ecozoneapi.payment.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * 결제 이벤트 도메인 모델 (Webhook용)
 * - PG로부터 수신한 이벤트 추적
 * - 멱등성 보장 (eventId 기반)
 * - 불변 객체
 * @since 2025-01-15
 */
@Getter
public final class PaymentEvent {

    private final Long id;
    private final String eventId;         // PG의 이벤트 ID (멱등키)
    private final EventType eventType;
    private final String orderId;
    private final String paymentKey;
    private final ProcessStatus status;
    private final String payload;         // 원본 JSON 페이로드
    private final String failureReason;
    private final Instant occurredAt;     // PG에서 발생한 시각
    private final Instant processedAt;    // 처리 완료 시각
    private final Instant createdAt;

    public PaymentEvent(
            Long id,
            String eventId,
            EventType eventType,
            String orderId,
            String paymentKey,
            ProcessStatus status,
            String payload,
            String failureReason,
            Instant occurredAt,
            Instant processedAt,
            Instant createdAt
    ) {
        this.id = id;
        this.eventId = Objects.requireNonNull(eventId, "eventId required");
        this.eventType = Objects.requireNonNull(eventType, "eventType required");
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.status = Objects.requireNonNull(status, "status required");
        this.payload = payload;
        this.failureReason = failureReason;
        this.occurredAt = occurredAt;
        this.processedAt = processedAt;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt required");
    }

    /**
     * 새로운 이벤트 생성 (PENDING)
     */
    public static PaymentEvent create(
            String eventId,
            EventType eventType,
            String orderId,
            String paymentKey,
            String payload,
            Instant occurredAt,
            Instant now
    ) {
        return new PaymentEvent(
                null,
                eventId,
                eventType,
                orderId,
                paymentKey,
                ProcessStatus.PENDING,
                payload,
                null,
                occurredAt,
                null,
                now
        );
    }

    /**
     * 처리 완료 상태로 전이
     */
    public PaymentEvent markProcessed(Instant now) {
        if (this.status == ProcessStatus.PROCESSED) {
            return this; // 멱등
        }
        return new PaymentEvent(
                this.id,
                this.eventId,
                this.eventType,
                this.orderId,
                this.paymentKey,
                ProcessStatus.PROCESSED,
                this.payload,
                null,
                this.occurredAt,
                now,
                this.createdAt
        );
    }

    /**
     * 처리 실패 상태로 전이
     */
    public PaymentEvent markFailed(String reason, Instant now) {
        if (this.status == ProcessStatus.PROCESSED) {
            return this; // 이미 처리된 이벤트는 실패로 되돌리지 않음
        }
        return new PaymentEvent(
                this.id,
                this.eventId,
                this.eventType,
                this.orderId,
                this.paymentKey,
                ProcessStatus.FAILED,
                this.payload,
                reason,
                this.occurredAt,
                null,
                this.createdAt
        );
    }

    public PaymentEvent withId(Long id) {
        return new PaymentEvent(
                id,
                this.eventId,
                this.eventType,
                this.orderId,
                this.paymentKey,
                this.status,
                this.payload,
                this.failureReason,
                this.occurredAt,
                this.processedAt,
                this.createdAt
        );
    }

    public boolean isPending() {
        return status == ProcessStatus.PENDING;
    }

    public boolean isProcessed() {
        return status == ProcessStatus.PROCESSED;
    }
}
