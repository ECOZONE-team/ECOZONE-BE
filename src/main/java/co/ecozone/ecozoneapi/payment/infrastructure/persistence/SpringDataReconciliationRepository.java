package co.ecozone.ecozoneapi.payment.infrastructure.persistence;

import co.ecozone.ecozoneapi.payment.domain.model.ReconciliationEntry;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity.ReconciliationEntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 조정 Spring Data JPA Repository
 * @since 2025-01-15
 */
public interface SpringDataReconciliationRepository extends JpaRepository<ReconciliationEntryJpaEntity, Long> {

    List<ReconciliationEntryJpaEntity> findByReconciliationBatchId(String batchId);

    @Query("SELECT r FROM ReconciliationEntryJpaEntity r WHERE r.status = 'MANUAL_REQUIRED' ORDER BY r.createdAt DESC")
    List<ReconciliationEntryJpaEntity> findManualRequiredEntries();

    @Query("SELECT r FROM ReconciliationEntryJpaEntity r WHERE r.status = 'DISCREPANCY' ORDER BY r.createdAt DESC")
    List<ReconciliationEntryJpaEntity> findDiscrepancies();

    @Query("SELECT r FROM ReconciliationEntryJpaEntity r WHERE r.orderId = :orderId ORDER BY r.createdAt DESC LIMIT 1")
    Optional<ReconciliationEntryJpaEntity> findLatestByOrderId(@Param("orderId") String orderId);

    @Query("SELECT r FROM ReconciliationEntryJpaEntity r WHERE r.createdAt BETWEEN :from AND :to ORDER BY r.createdAt ASC")
    List<ReconciliationEntryJpaEntity> findByDateRange(@Param("from") Instant from, @Param("to") Instant to);
}
