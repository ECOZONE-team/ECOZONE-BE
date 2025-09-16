package co.ecozone.ecozoneapi.payment.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * 결제 도메인 Aggregate Root
 * - 상태 전이(initiated → authorized → confirmed/canceled/failed) 불변식 유지
 * - 외부(영속/JPA, 웹) 모델과 분리된 순수 도메인
 * - 팩터리/전이 메서드로 유효성 보장, 값 객체/열거형으로 의도 명확화
 * @since 2025-09-16
 */
@Getter
public final class Payment {
    private final Long id;
    private final String orderId;      // 상점 내 주문번호
    private final long amount;         // 정수 금액(원)
    private final String paymentKey;   // PG 결제키
    private final PaymentStatus status;
    private final Instant approvedAt;
    private final Instant canceledAt;
    private final String failureReason;

    public Payment(Long id, String orderId, long amount, String paymentKey,
                   PaymentStatus status, Instant approvedAt, Instant canceledAt, String failureReason) {
        this.id = id;
        this.orderId = Objects.requireNonNull(orderId);
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.status = status;
        this.approvedAt = approvedAt;
        this.canceledAt = canceledAt;
        this.failureReason = failureReason;
    }

    public static Payment initiated(String orderId, long amount) {
        return new Payment(null, orderId, amount, null, PaymentStatus.INITIATED, null, null, null);
    }

    public Payment authorized(String paymentKey) {
        return new Payment(id, orderId, amount, paymentKey, PaymentStatus.AUTHORIZED, null, null, null);
    }

    public Payment confirmed(Instant at) {
        return new Payment(id, orderId, amount, paymentKey, PaymentStatus.CONFIRMED, at, null, null);
    }

    public Payment canceled(Instant at, String reason) {
        return new Payment(id, orderId, amount, paymentKey, PaymentStatus.CANCELED, approvedAt, at, reason);
    }

    public Payment failed(String reason) {
        return new Payment(id, orderId, amount, paymentKey, PaymentStatus.FAILED, approvedAt, canceledAt, reason);
    }

    public Payment withId(Long id) {
        return new Payment(id, orderId, amount, paymentKey, status, approvedAt, canceledAt, failureReason);
    }
}
