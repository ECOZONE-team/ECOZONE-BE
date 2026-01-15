package co.ecozone.ecozoneapi.payment.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * 결제 명령(Command) 도메인 모델
 * - 멱등성 보장을 위한 명령 추적
 * - 불변 객체로 상태 전이 관리
 * - CommandLog의 순수 도메인 표현
 * @since 2025-01-15
 */
@Getter
public final class PaymentCommand {

    public enum Status {
        IN_PROGRESS,  // 처리 중
        SUCCEEDED,    // 성공
        FAILED        // 실패
    }

    private final Long id;
    private final String idempotencyKey;  // 멱등키 (UNIQUE)
    private final String orderId;
    private final String requestHash;     // SHA-256(paymentKey|orderId|amount)
    private final Status status;
    private final Long paymentId;         // 성공 시 Payment ID
    private final String failureReason;
    private final Instant createdAt;
    private final Instant updatedAt;

    public PaymentCommand(
            Long id,
            String idempotencyKey,
            String orderId,
            String requestHash,
            Status status,
            Long paymentId,
            String failureReason,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "idempotencyKey required");
        this.orderId = Objects.requireNonNull(orderId, "orderId required");
        this.requestHash = Objects.requireNonNull(requestHash, "requestHash required");
        this.status = Objects.requireNonNull(status, "status required");
        this.paymentId = paymentId;
        this.failureReason = failureReason;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt required");
    }

    /**
     * 새로운 명령 시작 (IN_PROGRESS)
     */
    public static PaymentCommand start(String idempotencyKey, String orderId, String requestHash, Instant now) {
        if (requestHash.length() != 64) {
            throw new IllegalArgumentException("requestHash must be 64-length hex (SHA-256)");
        }
        return new PaymentCommand(null, idempotencyKey, orderId, requestHash, Status.IN_PROGRESS, null, null, now, now);
    }

    /**
     * 성공 상태로 전이
     * 멱등성: 이미 SUCCEEDED면 그대로 반환
     */
    public PaymentCommand markSucceeded(Long paymentId, Instant now) {
        if (this.status == Status.SUCCEEDED) {
            return this; // 멱등
        }
        return new PaymentCommand(
                this.id,
                this.idempotencyKey,
                this.orderId,
                this.requestHash,
                Status.SUCCEEDED,
                Objects.requireNonNull(paymentId, "paymentId required for success"),
                null,
                this.createdAt,
                now
        );
    }

    /**
     * 실패 상태로 전이
     * 불변식: 이미 SUCCEEDED인 명령은 FAILED로 되돌리지 않음
     */
    public PaymentCommand markFailed(String reason, Instant now) {
        if (this.status == Status.SUCCEEDED) {
            return this; // 성공한 명령은 실패로 되돌리지 않음
        }
        return new PaymentCommand(
                this.id,
                this.idempotencyKey,
                this.orderId,
                this.requestHash,
                Status.FAILED,
                null,
                reason,
                this.createdAt,
                now
        );
    }

    /**
     * ID 부여 (영속화 후)
     */
    public PaymentCommand withId(Long id) {
        return new PaymentCommand(
                id,
                this.idempotencyKey,
                this.orderId,
                this.requestHash,
                this.status,
                this.paymentId,
                this.failureReason,
                this.createdAt,
                this.updatedAt
        );
    }

    public boolean isSucceeded() {
        return status == Status.SUCCEEDED;
    }

    public boolean isFailed() {
        return status == Status.FAILED;
    }

    public boolean isInProgress() {
        return status == Status.IN_PROGRESS;
    }
}
