package co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity;

import co.ecozone.ecozoneapi.payment.domain.model.PaymentEvent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

/**
 * 결제 이벤트 JPA 엔티티 (Webhook용)
 * - PG로부터 수신한 이벤트 추적
 * - eventId UNIQUE로 중복 처리 방지
 * @since 2025-01-15
 */
@Entity
@Table(
        name = "payment_event",
        indexes = {
                @Index(name = "ux_event_id", columnList = "eventId", unique = true),
                @Index(name = "idx_order_id", columnList = "orderId"),
                @Index(name = "idx_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor
public class PaymentEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentEvent.EventType eventType;

    @Column(length = 191)
    private String orderId;

    @Column(length = 200)
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentEvent.ProcessStatus status;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(length = 1000)
    private String failureReason;

    private Instant occurredAt;

    private Instant processedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Version
    private Long version;

    public static PaymentEventJpaEntity from(PaymentEvent event) {
        var entity = new PaymentEventJpaEntity();
        entity.id = event.getId();
        entity.eventId = event.getEventId();
        entity.eventType = event.getEventType();
        entity.orderId = event.getOrderId();
        entity.paymentKey = event.getPaymentKey();
        entity.status = event.getStatus();
        entity.payload = event.getPayload();
        entity.failureReason = event.getFailureReason();
        entity.occurredAt = event.getOccurredAt();
        entity.processedAt = event.getProcessedAt();
        entity.createdAt = event.getCreatedAt();
        return entity;
    }

    public PaymentEvent toDomain() {
        return new PaymentEvent(
                this.id,
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

    public void applyFrom(PaymentEvent event) {
        this.status = event.getStatus();
        this.failureReason = event.getFailureReason();
        this.processedAt = event.getProcessedAt();
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = PaymentEvent.ProcessStatus.PENDING;
    }
}
