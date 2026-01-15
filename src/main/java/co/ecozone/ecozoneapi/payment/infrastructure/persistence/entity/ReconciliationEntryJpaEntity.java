package co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity;

import co.ecozone.ecozoneapi.payment.domain.model.PaymentStatus;
import co.ecozone.ecozoneapi.payment.domain.model.ReconciliationEntry;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 조정(Reconciliation) 결과 JPA 엔티티
 * - 내부 시스템과 PG 간 거래 대사 결과 저장
 * @since 2025-01-15
 */
@Entity
@Table(
        name = "reconciliation_entry",
        indexes = {
                @Index(name = "idx_batch_id", columnList = "reconciliationBatchId"),
                @Index(name = "idx_order_id", columnList = "orderId"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_created_at", columnList = "createdAt")
        }
)
@Getter
@NoArgsConstructor
public class ReconciliationEntryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 191)
    private String orderId;

    @Column(nullable = false, length = 100)
    private String reconciliationBatchId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReconciliationEntry.Status status;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ReconciliationEntry.DiscrepancyType discrepancyType;

    // 내부 시스템 정보
    private Long internalPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentStatus internalStatus;

    private Long internalAmount;

    // PG 정보
    @Column(length = 200)
    private String pgPaymentKey;

    @Column(length = 30)
    private String pgStatus;

    private Long pgAmount;

    // 조정 정보
    @Column(length = 500)
    private String resolution;

    @Column(columnDefinition = "TEXT")
    private String note;

    private Instant reconciledAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public static ReconciliationEntryJpaEntity from(ReconciliationEntry entry) {
        var entity = new ReconciliationEntryJpaEntity();
        entity.id = entry.getId();
        entity.orderId = entry.getOrderId();
        entity.reconciliationBatchId = entry.getReconciliationBatchId();
        entity.status = entry.getStatus();
        entity.discrepancyType = entry.getDiscrepancyType();
        entity.internalPaymentId = entry.getInternalPaymentId();
        entity.internalStatus = entry.getInternalStatus();
        entity.internalAmount = entry.getInternalAmount();
        entity.pgPaymentKey = entry.getPgPaymentKey();
        entity.pgStatus = entry.getPgStatus();
        entity.pgAmount = entry.getPgAmount();
        entity.resolution = entry.getResolution();
        entity.note = entry.getNote();
        entity.reconciledAt = entry.getReconciledAt();
        entity.createdAt = entry.getCreatedAt();
        entity.updatedAt = entry.getUpdatedAt();
        return entity;
    }

    public ReconciliationEntry toDomain() {
        return new ReconciliationEntry(
                this.id,
                this.orderId,
                this.reconciliationBatchId,
                this.status,
                this.discrepancyType,
                this.internalPaymentId,
                this.internalStatus,
                this.internalAmount,
                this.pgPaymentKey,
                this.pgStatus,
                this.pgAmount,
                this.resolution,
                this.note,
                this.reconciledAt,
                this.createdAt,
                this.updatedAt
        );
    }

    public void applyFrom(ReconciliationEntry entry, Instant now) {
        this.status = entry.getStatus();
        this.discrepancyType = entry.getDiscrepancyType();
        this.resolution = entry.getResolution();
        this.note = entry.getNote();
        this.reconciledAt = entry.getReconciledAt();
        this.updatedAt = now;
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
