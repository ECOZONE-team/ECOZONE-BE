package co.ecozone.ecozoneapi.payment.application.port.out;

import co.ecozone.ecozoneapi.payment.domain.model.ReconciliationEntry;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 조정(Reconciliation) 저장소 포트
 * - 내부 시스템과 PG 간 대사 결과 저장
 * @since 2025-01-15
 */
public interface ReconciliationRepository {

    /**
     * 조정 결과 저장
     */
    ReconciliationEntry save(ReconciliationEntry entry);

    /**
     * 배치별 조정 결과 조회
     */
    List<ReconciliationEntry> findByBatchId(String batchId);

    /**
     * 수동 조정 필요 건 조회
     */
    List<ReconciliationEntry> findManualRequiredEntries();

    /**
     * 불일치 건 조회
     */
    List<ReconciliationEntry> findDiscrepancies();

    /**
     * orderId로 최신 조정 결과 조회
     */
    Optional<ReconciliationEntry> findLatestByOrderId(String orderId);

    /**
     * 기간별 조정 결과 조회
     */
    List<ReconciliationEntry> findByDateRange(Instant from, Instant to);
}
