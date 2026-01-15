package co.ecozone.ecozoneapi.payment.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * 조정(Reconciliation) 결과 도메인 모델
 * - 내부 시스템과 PG 간 거래 대사 결과
 * - 불일치 건 추적 및 자동/수동 조정 처리
 * - 불변 객체
 * @since 2025-01-15
 */
@Getter
public final class ReconciliationEntry {

    public enum Status {
        MATCHED,           // 일치
        DISCREPANCY,       // 불일치
        MISSING_IN_PG,     // PG에 없음
        MISSING_IN_SYSTEM, // 내부 시스템에 없음
        AUTO_RESOLVED,     // 자동 조정 완료
        MANUAL_REQUIRED    // 수동 조정 필요
    }

    public enum DiscrepancyType {
        AMOUNT_MISMATCH,   // 금액 불일치
        STATUS_MISMATCH,   // 상태 불일치
        MISSING,           // 누락
        DUPLICATE          // 중복
    }

    private final Long id;
    private final String orderId;
    private final String reconciliationBatchId; // 배치 실행 ID
    private final Status status;
    private final DiscrepancyType discrepancyType;

    // 내부 시스템 정보
    private final Long internalPaymentId;
    private final PaymentStatus internalStatus;
    private final Long internalAmount;

    // PG 정보
    private final String pgPaymentKey;
    private final String pgStatus;
    private final Long pgAmount;

    // 조정 정보
    private final String resolution;         // 조정 방법/결과
    private final String note;                // 비고
    private final Instant reconciledAt;       // 조정 완료 시각
    private final Instant createdAt;
    private final Instant updatedAt;

    public ReconciliationEntry(
            Long id,
            String orderId,
            String reconciliationBatchId,
            Status status,
            DiscrepancyType discrepancyType,
            Long internalPaymentId,
            PaymentStatus internalStatus,
            Long internalAmount,
            String pgPaymentKey,
            String pgStatus,
            Long pgAmount,
            String resolution,
            String note,
            Instant reconciledAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.orderId = Objects.requireNonNull(orderId, "orderId required");
        this.reconciliationBatchId = Objects.requireNonNull(reconciliationBatchId, "batchId required");
        this.status = Objects.requireNonNull(status, "status required");
        this.discrepancyType = discrepancyType;
        this.internalPaymentId = internalPaymentId;
        this.internalStatus = internalStatus;
        this.internalAmount = internalAmount;
        this.pgPaymentKey = pgPaymentKey;
        this.pgStatus = pgStatus;
        this.pgAmount = pgAmount;
        this.resolution = resolution;
        this.note = note;
        this.reconciledAt = reconciledAt;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt required");
    }

    /**
     * 일치하는 거래 생성
     */
    public static ReconciliationEntry matched(
            String orderId,
            String batchId,
            Long paymentId,
            PaymentStatus internalStatus,
            long amount,
            String pgPaymentKey,
            String pgStatus,
            Instant now
    ) {
        return new ReconciliationEntry(
                null,
                orderId,
                batchId,
                Status.MATCHED,
                null,
                paymentId,
                internalStatus,
                amount,
                pgPaymentKey,
                pgStatus,
                amount,
                null,
                null,
                null,
                now,
                now
        );
    }

    /**
     * 불일치 건 생성
     */
    public static ReconciliationEntry discrepancy(
            String orderId,
            String batchId,
            DiscrepancyType type,
            Long paymentId,
            PaymentStatus internalStatus,
            Long internalAmount,
            String pgPaymentKey,
            String pgStatus,
            Long pgAmount,
            String note,
            Instant now
    ) {
        return new ReconciliationEntry(
                null,
                orderId,
                batchId,
                Status.DISCREPANCY,
                type,
                paymentId,
                internalStatus,
                internalAmount,
                pgPaymentKey,
                pgStatus,
                pgAmount,
                null,
                note,
                null,
                now,
                now
        );
    }

    /**
     * 자동 조정 완료
     */
    public ReconciliationEntry autoResolve(String resolution, Instant now) {
        return new ReconciliationEntry(
                this.id,
                this.orderId,
                this.reconciliationBatchId,
                Status.AUTO_RESOLVED,
                this.discrepancyType,
                this.internalPaymentId,
                this.internalStatus,
                this.internalAmount,
                this.pgPaymentKey,
                this.pgStatus,
                this.pgAmount,
                resolution,
                this.note,
                now,
                this.createdAt,
                now
        );
    }

    /**
     * 수동 조정 필요로 표시
     */
    public ReconciliationEntry markManualRequired(String note, Instant now) {
        return new ReconciliationEntry(
                this.id,
                this.orderId,
                this.reconciliationBatchId,
                Status.MANUAL_REQUIRED,
                this.discrepancyType,
                this.internalPaymentId,
                this.internalStatus,
                this.internalAmount,
                this.pgPaymentKey,
                this.pgStatus,
                this.pgAmount,
                null,
                note,
                null,
                this.createdAt,
                now
        );
    }

    public ReconciliationEntry withId(Long id) {
        return new ReconciliationEntry(
                id,
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

    public boolean isMatched() {
        return status == Status.MATCHED;
    }

    public boolean hasDiscrepancy() {
        return status == Status.DISCREPANCY;
    }

    public boolean requiresManualAction() {
        return status == Status.MANUAL_REQUIRED || status == Status.DISCREPANCY;
    }
}
