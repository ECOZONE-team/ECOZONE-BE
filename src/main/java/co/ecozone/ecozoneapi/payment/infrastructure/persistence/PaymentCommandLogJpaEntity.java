package co.ecozone.ecozoneapi.payment.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 지급 명령(Idempotency) 로그 JPA 엔티티
 * - idemKey UNIQUE로 동일 요청의 중복 처리 방지
 * - 상태(IN_PROGRESS/SUCCEEDED/FAILED)와 해시/결제ID/타임스탬프 저장
 * @since 2025-09-16
 */
@Entity
@Table(name = "payment_command_log",
        indexes = { @Index(name = "ux_idem_key", columnList = "idemKey", unique = true) })
@Getter
@NoArgsConstructor
public class PaymentCommandLogJpaEntity {

    public enum Status { IN_PROGRESS, SUCCEEDED, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, unique = true)
    private String idemKey;

    @Column(nullable = false, length = 100)
    private String orderId;

    @Column(nullable = false, length = 64)
    private String requestHash;     // paymentKey+orderId+amount 해시 등

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Status status;

    private Long paymentId;

    @Column(length = 1000)
    private String failureReason;

    private Instant createdAt;

    private Instant updatedAt;

    @Version
    private Long version;

    public static PaymentCommandLogJpaEntity start(String idemKey, String orderId, String requestHash, Instant now) {
        var e = new PaymentCommandLogJpaEntity();
        e.idemKey = idemKey;
        e.orderId = orderId;
        e.requestHash = requestHash;
        e.status = Status.IN_PROGRESS;
        e.createdAt = now;
        e.updatedAt = now;
        return e;
    }

    public void markSucceeded(Long paymentId, Instant now) {
        this.status = Status.SUCCEEDED;
        this.paymentId = paymentId;
        this.updatedAt = now;
    }

    public void markFailed(String reason, Instant now) {
        this.status = Status.FAILED;
        this.failureReason = reason;
        this.updatedAt = now;
    }
}
