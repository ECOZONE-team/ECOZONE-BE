package co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity;

import co.ecozone.ecozoneapi.payment.domain.model.PaymentCommand;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

/**
 * 지급 명령(Idempotency) 로그 JPA 엔티티
 * - idemKey UNIQUE로 동일 요청의 중복 처리 방지
 * - 상태(IN_PROGRESS/SUCCEEDED/FAILED)와 해시/결제ID/타임스탬프 저장
 * - PaymentCommand 도메인 모델과 연동
 * @since 2025-09-16
 */
@Entity
@Table(
        name = "payment_command_log",
        indexes = {
            @Index(name = "ux_idem_key", columnList = "idemKey", unique = true),
            @Index(name = "idx_order_id", columnList = "orderId")
        }
)
@Getter
@NoArgsConstructor
public class PaymentCommandLogJpaEntity {

    @Deprecated(since = "2025-01-15", forRemoval = true)
    public enum Status { IN_PROGRESS, SUCCEEDED, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 클라이언트가 보낸 멱등키(헤더) */
    @Column(nullable = false, length = 120, unique = true)
    private String idemKey;

    /** 상점 주문번호 */
    @Column(nullable = false, length = 191)
    private String orderId;

    /** 요청 고유성 보장용 해시(paymentKey|orderId|amount) – 64자 고정(SHA-256 hex) */
    @Column(nullable = false, length = 64)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    /** 이 커맨드가 최종 반영한 Payment PK (성공 시 세팅) */
    private Long paymentId;

    @Column(length = 1000)
    private String failureReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    /**
     * 도메인 모델로부터 JPA 엔티티 생성
     */
    public static PaymentCommandLogJpaEntity from(PaymentCommand command) {
        var entity = new PaymentCommandLogJpaEntity();
        entity.id = command.getId();
        entity.idemKey = command.getIdempotencyKey();
        entity.orderId = command.getOrderId();
        entity.requestHash = command.getRequestHash();
        entity.status = mapStatus(command.getStatus());
        entity.paymentId = command.getPaymentId();
        entity.failureReason = command.getFailureReason();
        entity.createdAt = command.getCreatedAt();
        entity.updatedAt = command.getUpdatedAt();
        return entity;
    }

    /**
     * JPA 엔티티를 도메인 모델로 변환
     */
    public PaymentCommand toDomain() {
        return new PaymentCommand(
                this.id,
                this.idemKey,
                this.orderId,
                this.requestHash,
                mapDomainStatus(this.status),
                this.paymentId,
                this.failureReason,
                this.createdAt,
                this.updatedAt
        );
    }

    /**
     * 도메인 모델의 변경사항을 JPA 엔티티에 반영
     */
    public void applyFrom(PaymentCommand command, Instant now) {
        this.status = mapStatus(command.getStatus());
        this.paymentId = command.getPaymentId();
        this.failureReason = command.getFailureReason();
        this.updatedAt = now;
    }

    private static Status mapStatus(PaymentCommand.Status domainStatus) {
        return switch (domainStatus) {
            case IN_PROGRESS -> Status.IN_PROGRESS;
            case SUCCEEDED -> Status.SUCCEEDED;
            case FAILED -> Status.FAILED;
        };
    }

    private static PaymentCommand.Status mapDomainStatus(Status jpaStatus) {
        return switch (jpaStatus) {
            case IN_PROGRESS -> PaymentCommand.Status.IN_PROGRESS;
            case SUCCEEDED -> PaymentCommand.Status.SUCCEEDED;
            case FAILED -> PaymentCommand.Status.FAILED;
        };
    }

    // 레거시 메서드 (하위 호환성 유지)
    @Deprecated(since = "2025-01-15", forRemoval = true)
    public static PaymentCommandLogJpaEntity start(String idemKey, String orderId, String requestHash, Instant now) {
        var e = new PaymentCommandLogJpaEntity();
        e.idemKey = Objects.requireNonNull(idemKey, "idemKey");
        e.orderId = Objects.requireNonNull(orderId, "orderId");
        e.requestHash = Objects.requireNonNull(requestHash, "requestHash");
        if (requestHash.length() != 64) {
            throw new IllegalArgumentException("requestHash must be 64-length hex");
        }
        e.status = Status.IN_PROGRESS;
        e.createdAt = Objects.requireNonNull(now, "now");
        e.updatedAt = now;
        return e;
    }

    @Deprecated(since = "2025-01-15", forRemoval = true)
    public void markSucceeded(Long paymentId, Instant now) {
        if (this.status == Status.SUCCEEDED) return; // 멱등
        this.status = Status.SUCCEEDED;
        this.paymentId = Objects.requireNonNull(paymentId, "paymentId");
        this.updatedAt = Objects.requireNonNull(now, "now");
    }

    @Deprecated(since = "2025-01-15", forRemoval = true)
    public void markFailed(String reason, Instant now) {
        if (this.status == Status.SUCCEEDED) return; // 이미 성공한 커맨드는 실패로 되돌리지 않음
        this.status = Status.FAILED;
        this.failureReason = reason;
        this.updatedAt = Objects.requireNonNull(now, "now");
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = Status.IN_PROGRESS;
        if (requestHash != null && requestHash.length() != 64) {
            throw new IllegalStateException("requestHash must be 64-length hex");
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}

