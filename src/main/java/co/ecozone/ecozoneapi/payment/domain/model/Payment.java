package co.ecozone.ecozoneapi.payment.domain.model;

import co.ecozone.ecozoneapi.auth.domain.model.UserId;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * 결제 도메인 Aggregate Root
 * - 상태 전이(initiated → authorized → confirmed/canceled/failed) 불변식 유지
 * - 외부(영속/JPA, 웹) 모델과 분리된 순수 도메인
 * - 팩터리/전이 메서드로 유효성 보장
 * @since 2025-09-16
 */
@Getter
public final class Payment {
    private final Long id;
    private final String orderId;     // 상점 내 주문번호 (멱등 단위)
    private final long amount;        // 정수 금액(원)
    private final UserId userId;      // 주문자
    private final String paymentKey;  // PG 결제키(authorized 이후 채워짐)
    private final PaymentStatus status;
    private final Instant approvedAt;
    private final Instant canceledAt;
    private final String failureReason;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Payment(
            Long id, String orderId, long amount, UserId userId, String paymentKey,
            PaymentStatus status, Instant approvedAt, Instant canceledAt, String failureReason,
            Instant createdAt, Instant updatedAt
    ) {
        this.id = id;
        this.orderId = Objects.requireNonNull(orderId);
        this.amount = amount;
        this.userId = Objects.requireNonNull(userId);
        this.paymentKey = paymentKey;
        this.status = Objects.requireNonNull(status);
        this.approvedAt = approvedAt;
        this.canceledAt = canceledAt;
        this.failureReason = failureReason;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Payment initiated(UserId userId, String orderId, long amount, Instant now) {
        return new Payment(null, orderId, amount, userId, null,
                PaymentStatus.INITIATED, null, null, null, now, now);
    }

    public Payment authorized(String paymentKey, Instant now) {
        if (!status.canTransitionTo(PaymentStatus.AUTHORIZED))
            throw new IllegalStateException("invalid transition: " + status + " -> AUTHORIZED");
        return new Payment(id, orderId, amount, userId, paymentKey,
                PaymentStatus.AUTHORIZED, null, null, null, createdAt, now);
    }

    public Payment confirmed(Instant approvedAt, Instant now) {
        if (!status.canTransitionTo(PaymentStatus.CONFIRMED))
            throw new IllegalStateException("invalid transition: " + status + " -> CONFIRMED");
        return new Payment(id, orderId, amount, userId, paymentKey,
                PaymentStatus.CONFIRMED, approvedAt, null, null, createdAt, now);
    }

    public Payment canceled(String reason, Instant now) {
        if (!status.canTransitionTo(PaymentStatus.CANCELED))
            throw new IllegalStateException("invalid transition: " + status + " -> CANCELED");
        return new Payment(id, orderId, amount, userId, paymentKey,
                PaymentStatus.CANCELED, approvedAt, now, reason, createdAt, now);
    }

    public Payment failed(String reason, Instant now) {
        if (!status.canTransitionTo(PaymentStatus.FAILED))
            throw new IllegalStateException("invalid transition: " + status + " -> FAILED");
        return new Payment(id, orderId, amount, userId, paymentKey,
                PaymentStatus.FAILED, approvedAt, canceledAt, reason, createdAt, now);
    }

    public boolean isTerminal() {
        return status.isTerminal();
    }

    public Payment withId(Long id, Instant now) {
        return new Payment(id, orderId, amount, userId, paymentKey, status,
                approvedAt, canceledAt, failureReason, createdAt, now);
    }
}

