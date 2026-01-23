package co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity;

import co.ecozone.ecozoneapi.auth.domain.model.UserId;
import co.ecozone.ecozoneapi.payment.domain.model.Payment;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 결제 JPA 엔티티 (영속 모델)
 * - @Version long 으로 동시성 제어, null 버전 방지
 * - 승인/취소/실패 시각 및 사유 컬럼 분리, NOT NULL 제약 적정화
 * - 도메인 변환 메서드(fromNew/applyFrom/toDomain) 제공
 * @since 2025-09-16
 */
@Getter
@Entity
@NoArgsConstructor
@Table(name = "payment",
        indexes = {
                @Index(name = "ux_payment_order", columnList = "orderId", unique = true),
                @Index(name = "idx_payment_userid", columnList = "userId"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_created", columnList = "createdAt"),
                @Index(name = "idx_payment_userid_status", columnList = "userId,status"),
                @Index(name = "idx_payment_created_status", columnList = "createdAt,status")
        })
public class PaymentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String orderId;

    @Column(nullable=false)
    private long amount;

    @Column(nullable=false)
    private Long userId;

    @Column(length = 200)         // NULL 허용, PG 승인 후 채워짐
    private String paymentKey;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Instant approvedAt;

    private Instant canceledAt;

    @Column(length = 1000)
    private String failureReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    /** 낙관적 락 */
    @Version
    private Long version;

    public static PaymentJpaEntity newOf(Payment p) {
        var e = new PaymentJpaEntity();
        e.orderId = p.getOrderId();
        e.amount = p.getAmount();
        e.userId = p.getUserId().value();
        e.paymentKey = p.getPaymentKey();
        e.status = p.getStatus();
        e.approvedAt = p.getApprovedAt();
        e.canceledAt = p.getCanceledAt();
        e.failureReason = p.getFailureReason();
        return e;
    }

    public void applyFrom(Payment p, Instant now) {
        this.amount = p.getAmount();
        this.userId = p.getUserId().value();
        this.paymentKey = p.getPaymentKey();
        this.status = p.getStatus();
        this.approvedAt = p.getApprovedAt();
        this.canceledAt = p.getCanceledAt();
        this.failureReason = p.getFailureReason();
        this.updatedAt = now;
    }

    public Payment toDomain() {
        return new Payment(
                id,
                orderId,
                amount,
                new UserId(userId),
                paymentKey,
                status,
                approvedAt,
                canceledAt,
                failureReason,
                createdAt,
                updatedAt
        );
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = PaymentStatus.INITIATED;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

}